package moh.gov.zm.lis.lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.exception.ValidationException;
import moh.gov.zm.lis.lab.dto.BulkUploadDTO;
import moh.gov.zm.lis.lab.entity.FacilityLaboratoryMap;
import moh.gov.zm.lis.lab.entity.Laboratory;
import moh.gov.zm.lis.lab.entity.LaboratoryTest;
import moh.gov.zm.lis.lab.entity.Test;
import moh.gov.zm.lis.lab.repository.FacilityLaboratoryMapRepository;
import moh.gov.zm.lis.lab.repository.LaboratoryTestRepository;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Excel-based bulk maintenance of {@code lab.facility_laboratory_map}. Generates
 * a data-validated template (dropdowns of the live mfl / loinc / lab codes) and
 * ingests a filled-in workbook, resolving each {@code (mfl, loinc, lab)} row to a
 * facility and an existing laboratory-test offering and upserting the mapping.
 *
 * <p>Reference data (facilities, tests, laboratories) is resolved from the
 * Redis-cached snapshots via the respective services, so a large upload does not
 * hammer the database for rarely-changing lookups. The mutable
 * {@code laboratory_test} and {@code facility_laboratory_map} rows are always read
 * fresh from the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityLaboratoryMapBulkService {
    private static final String SHEET_MAIN = "Mappings";
    private static final String SHEET_FACILITIES = "facilities";
    private static final String SHEET_TESTS = "tests";
    private static final String SHEET_LABS = "laboratories";
    private static final String[] HEADERS = {"mfl_code", "loinc_code", "lab_code"};
    /** Rows (below the header) the dropdowns are applied to. */
    private static final int VALIDATED_ROWS = 2000;

    private final FacilityService facilityService;
    private final TestService testService;
    private final LaboratoryService laboratoryService;
    private final LaboratoryTestRepository laboratoryTestRepository;
    private final FacilityLaboratoryMapRepository facilityLaboratoryMapRepository;

    // ---------------------------------------------------------------- template

    public Mono<byte[]> generateTemplate() {
        return Mono.zip(
                        facilityService.cachedAll(),
                        testService.cachedAll(),
                        laboratoryService.cachedAll())
                .flatMap(t -> Mono.fromCallable(() -> buildTemplate(t))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private byte[] buildTemplate(Tuple3<List<Facility>, List<Test>, List<Laboratory>> data) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet main = wb.createSheet(SHEET_MAIN);

            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            Row header = main.createRow(0);
            for (int c = 0; c < HEADERS.length; c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(HEADERS[c]);
                cell.setCellStyle(headerStyle);
                main.setColumnWidth(c, 22 * 256);
            }
            main.createFreezePane(0, 1);

            int mflCount = writeReferenceSheet(wb, SHEET_FACILITIES, "mfl_code", "facility_name",
                    data.getT1(), Facility::getMflCode, Facility::getName);
            int loincCount = writeReferenceSheet(wb, SHEET_TESTS, "loinc_code", "test_name",
                    data.getT2(), Test::getLoincCode, Test::getName);
            int labCount = writeReferenceSheet(wb, SHEET_LABS, "lab_code", "lab_name",
                    data.getT3(), Laboratory::getLabCode, Laboratory::getLabName);

            defineNamedRange(wb, "mfl_codes", SHEET_FACILITIES, mflCount);
            defineNamedRange(wb, "loinc_codes", SHEET_TESTS, loincCount);
            defineNamedRange(wb, "lab_codes", SHEET_LABS, labCount);

            addDropdown(main, "mfl_codes", 0, mflCount);
            addDropdown(main, "loinc_codes", 1, loincCount);
            addDropdown(main, "lab_codes", 2, labCount);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private <T> int writeReferenceSheet(XSSFWorkbook wb, String name, String codeHeader, String labelHeader,
                                        List<T> rows, Function<T, String> code, Function<T, String> label) {
        XSSFSheet sheet = wb.createSheet(name);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue(codeHeader);
        header.createCell(1).setCellValue(labelHeader);

        List<T> sorted = rows.stream()
                .filter(row -> code.apply(row) != null && !code.apply(row).isBlank())
                .sorted(Comparator.comparing(code, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        int written = 0;
        for (T row : sorted) {
            Row r = sheet.createRow(written + 1);
            r.createCell(0).setCellValue(code.apply(row));
            r.createCell(1).setCellValue(label.apply(row) == null ? "" : label.apply(row));
            written++;
        }
        sheet.setColumnWidth(0, 22 * 256);
        sheet.setColumnWidth(1, 40 * 256);
        return written;
    }

    private void defineNamedRange(XSSFWorkbook wb, String rangeName, String sheetName, int count) {
        if (count <= 0) {
            return;
        }
        var namedRange = wb.createName();
        namedRange.setNameName(rangeName);
        namedRange.setRefersToFormula("%s!$A$2:$A$%d".formatted(sheetName, count + 1));
    }

    private void addDropdown(XSSFSheet sheet, String rangeName, int column, int count) {
        if (count <= 0) {
            return;
        }
        XSSFDataValidationHelper helper = new XSSFDataValidationHelper(sheet);
        DataValidationConstraint constraint = helper.createFormulaListConstraint(rangeName);
        CellRangeAddressList addresses = new CellRangeAddressList(1, VALIDATED_ROWS, column, column);
        DataValidation validation = helper.createValidation(constraint, addresses);
        validation.setSuppressDropDownArrow(true); // XSSF: required to actually render the arrow
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("Invalid value", "Choose a value from the dropdown list.");
        sheet.addValidationData(validation);
    }

    // ------------------------------------------------------------------ upload

    public Mono<BulkUploadDTO.BulkUploadSummary> process(byte[] workbook) {
        return Mono.fromCallable(() -> parse(workbook))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::resolveAndApply);
    }

    private List<ParsedRow> parse(byte[] bytes) {
        List<ParsedRow> rows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheet(SHEET_MAIN);
            if (sheet == null) {
                sheet = wb.getSheetAt(0);
            }
            DataFormatter formatter = new DataFormatter();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String mfl = cellValue(formatter, row, 0);
                String loinc = cellValue(formatter, row, 1);
                String lab = cellValue(formatter, row, 2);
                if (mfl.isEmpty() && loinc.isEmpty() && lab.isEmpty()) {
                    continue;
                }
                rows.add(new ParsedRow(r + 1, mfl, loinc, lab)); // 1-based sheet row number
            }
        } catch (Exception e) {
            log.warn("Failed to parse bulk-upload workbook: {}", e.getMessage());
            throw new ValidationException("Could not read the uploaded file as an .xlsx workbook",
                    Map.of("file", "Unreadable or not a valid Excel (.xlsx) file"));
        }
        return rows;
    }

    private static String cellValue(DataFormatter formatter, Row row, int column) {
        Cell cell = row.getCell(column);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private Mono<BulkUploadDTO.BulkUploadSummary> resolveAndApply(List<ParsedRow> rows) {
        if (rows.isEmpty()) {
            return Mono.just(emptySummary());
        }

        Mono<Map<String, Facility>> facilities = facilityService.cachedAll()
                .map(list -> list.stream()
                        .filter(f -> f.getMflCode() != null && !f.getMflCode().isBlank())
                        .collect(Collectors.toMap(Facility::getMflCode, Function.identity(), (a, b) -> a)));
        Mono<Map<String, Test>> tests = testService.cachedAll()
                .map(list -> list.stream()
                        .collect(Collectors.toMap(Test::getLoincCode, Function.identity(), (a, b) -> a)));
        Mono<Map<String, Laboratory>> laboratories = laboratoryService.cachedAll()
                .map(list -> list.stream()
                        .collect(Collectors.toMap(Laboratory::getLabCode, Function.identity(), (a, b) -> a)));

        return Mono.zip(facilities, tests, laboratories).flatMap(refs -> {
            Map<String, Facility> facByMfl = refs.getT1();
            Map<String, Test> testByLoinc = refs.getT2();
            Map<String, Laboratory> labByCode = refs.getT3();

            // Scope the mutable-table reads to only the labs / facilities referenced in this file.
            Set<Short> labIds = rows.stream()
                    .map(r -> labByCode.get(r.lab()))
                    .filter(Objects::nonNull)
                    .map(Laboratory::getId)
                    .collect(Collectors.toSet());
            Set<Long> facilityIds = rows.stream()
                    .map(r -> facByMfl.get(r.mfl()))
                    .filter(Objects::nonNull)
                    .map(Facility::getId)
                    .collect(Collectors.toSet());

            Mono<Map<String, LaboratoryTest>> offerings = labIds.isEmpty()
                    ? Mono.just(Map.of())
                    : laboratoryTestRepository.findAllByLaboratoryIdIn(labIds)
                    .collectMap(lt -> offeringKey(lt.getLaboratoryId(), lt.getTestId()));
            Mono<Map<String, FacilityLaboratoryMap>> existing = facilityIds.isEmpty()
                    ? Mono.just(Map.of())
                    : facilityLaboratoryMapRepository.findAllByFacilityIdIn(facilityIds)
                    .collectMap(m -> mappingKey(m.getFacilityId(), m.getLaboratoryTestId()));

            return Mono.zip(offerings, existing)
                    .flatMap(t -> classifyAndWrite(rows, facByMfl, testByLoinc, labByCode, t.getT1(), t.getT2()));
        });
    }

    private Mono<BulkUploadDTO.BulkUploadSummary> classifyAndWrite(
            List<ParsedRow> rows,
            Map<String, Facility> facByMfl,
            Map<String, Test> testByLoinc,
            Map<String, Laboratory> labByCode,
            Map<String, LaboratoryTest> offeringByKey,
            Map<String, FacilityLaboratoryMap> existingByKey) {

        List<BulkUploadDTO.RowError> errors = new ArrayList<>();
        List<FacilityLaboratoryMap> toCreate = new ArrayList<>();
        List<FacilityLaboratoryMap> toReactivate = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        OffsetDateTime now = OffsetDateTime.now();
        int skippedExisting = 0;
        int duplicates = 0;

        for (ParsedRow row : rows) {
            if (row.mfl().isEmpty() || row.loinc().isEmpty() || row.lab().isEmpty()) {
                errors.add(error(row, "Missing mfl_code, loinc_code or lab_code"));
                continue;
            }
            Facility facility = facByMfl.get(row.mfl());
            if (facility == null) {
                errors.add(error(row, "Unknown mfl_code: " + row.mfl()));
                continue;
            }
            Laboratory laboratory = labByCode.get(row.lab());
            if (laboratory == null) {
                errors.add(error(row, "Unknown lab_code: " + row.lab()));
                continue;
            }
            Test test = testByLoinc.get(row.loinc());
            if (test == null) {
                errors.add(error(row, "Unknown loinc_code: " + row.loinc()));
                continue;
            }
            LaboratoryTest offering = offeringByKey.get(offeringKey(laboratory.getId(), test.getId()));
            if (offering == null) {
                errors.add(error(row, "Laboratory '%s' does not offer test '%s'".formatted(row.lab(), row.loinc())));
                continue;
            }

            String key = mappingKey(facility.getId(), offering.getId());
            if (!seen.add(key)) {
                duplicates++;
                continue;
            }

            FacilityLaboratoryMap current = existingByKey.get(key);
            if (current != null) {
                if (Boolean.TRUE.equals(current.getIsActive())) {
                    skippedExisting++;
                } else {
                    current.setIsActive(true);
                    current.setUpdatedAt(now);
                    toReactivate.add(current);
                }
            } else {
                toCreate.add(FacilityLaboratoryMap.builder()
                        .facilityId(facility.getId())
                        .laboratoryTestId(offering.getId())
                        .isActive(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
            }
        }

        BulkUploadDTO.BulkUploadSummary summary = BulkUploadDTO.BulkUploadSummary.builder()
                .totalRows(rows.size())
                .created(toCreate.size())
                .reactivated(toReactivate.size())
                .skippedExisting(skippedExisting)
                .duplicateRows(duplicates)
                .errorRows(errors.size())
                .errors(errors.isEmpty() ? null : errors)
                .build();

        return Flux.concat(
                        facilityLaboratoryMapRepository.saveAll(toCreate),
                        facilityLaboratoryMapRepository.saveAll(toReactivate))
                .then(Mono.just(summary));
    }

    private static String offeringKey(Short laboratoryId, Long testId) {
        return laboratoryId + ":" + testId;
    }

    private static String mappingKey(Long facilityId, java.util.UUID laboratoryTestId) {
        return facilityId + ":" + laboratoryTestId;
    }

    private static BulkUploadDTO.RowError error(ParsedRow row, String reason) {
        return BulkUploadDTO.RowError.builder()
                .row(row.rowNumber())
                .mflCode(emptyToNull(row.mfl()))
                .loincCode(emptyToNull(row.loinc()))
                .labCode(emptyToNull(row.lab()))
                .reason(reason)
                .build();
    }

    private static String emptyToNull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    private static BulkUploadDTO.BulkUploadSummary emptySummary() {
        return BulkUploadDTO.BulkUploadSummary.builder()
                .totalRows(0)
                .created(0)
                .reactivated(0)
                .skippedExisting(0)
                .duplicateRows(0)
                .errorRows(0)
                .build();
    }

    private record ParsedRow(int rowNumber, String mfl, String loinc, String lab) {
    }
}
