package moh.gov.zm.lis.lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.lab.entity.Laboratory;
import moh.gov.zm.lis.lab.entity.ShippingOrder;
import moh.gov.zm.lis.lab.repository.ShippingOrderRepository;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Builds the facility shipping list as an .xlsx laid out like the Ministry of Health
 * sample: coat-of-arms header, a From (facility) / Refer To (lab) line, the order
 * table (S/N, Lab No, Full names, Age, Sex, Test Type, Requested date, Specimen
 * collected date) and a courier sign-off footer. Scoped to one facility and one lab
 * over an order-received window (default the last day). Apache POI only — no external
 * reporting engine is needed for this fixed spreadsheet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingListService {
    private static final String[] HEADERS = {
            "S/N", "Lab No", "Full names", "Age", "Sex", "Test Type", "Requested date", "Specimen collected date"
    };
    private static final String[] SIGN_OFF_ROLES = {
            "Sender (Facility)", "Courier", "Receiver (Hub Lab)", "Sender (Hub lab)", "Receiver (Pcr Lab)"
    };
    private static final String[] SIGN_OFF_COLS = {"No. of samples", "Date", "Time", "Name", "Signature"};
    private static final int COLS = HEADERS.length; // A..H

    private final ShippingOrderRepository shippingOrderRepository;
    private final FacilityService facilityService;
    private final LaboratoryService laboratoryService;

    public Mono<byte[]> generate(String mflCode, String labCode, LocalDate from, LocalDate to) {
        LocalDate effTo = to != null ? to : LocalDate.now(ZoneOffset.UTC);
        LocalDate effFrom = from != null ? from : effTo; // default window = 1 day
        OffsetDateTime fromTs = effFrom.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toTs = effTo.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        Mono<String> facilityName = facilityService.cachedAll().map(list -> list.stream()
                .filter(f -> mflCode.equals(f.getMflCode())).map(Facility::getName).findFirst().orElse(mflCode));
        Mono<String> labName = laboratoryService.cachedAll().map(list -> list.stream()
                .filter(l -> labCode.equals(l.getLabCode())).map(Laboratory::getLabName).findFirst().orElse(labCode));
        Mono<List<ShippingOrder>> orders = shippingOrderRepository
                .findByMflCodeAndLabCodeAndCreatedAtBetweenOrderByCreatedAtAsc(mflCode, labCode, fromTs, toTs)
                .collectList();

        return Mono.zip(facilityName, labName, orders)
                .flatMap(t -> Mono.fromCallable(() -> build(t.getT1(), t.getT2(), t.getT3()))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private byte[] build(String facilityName, String labName, List<ShippingOrder> orders) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Shipping List");
            for (int c = 0; c < COLS; c++) {
                sheet.setColumnWidth(c, c == 2 ? 28 * 256 : 18 * 256); // wider "Full names"
            }

            CellStyle title = centered(wb, 16, true);
            CellStyle subtitle = centered(wb, 12, true);
            CellStyle label = bold(wb);
            CellStyle header = headerStyle(wb);
            CellStyle cell = borderedStyle(wb);

            addCoatOfArms(wb, sheet);

            int r = 0;
            titleRow(sheet, r++, "Republic of Zambia", subtitle);
            titleRow(sheet, r++, "Ministry of Health", subtitle);
            titleRow(sheet, r++, "Shipping List", title);
            r++; // spacer

            // From (facility) ... Refer To (lab)
            Row meta = sheet.createRow(r++);
            text(meta, 0, "From", label);
            text(meta, 1, facilityName, label);
            sheet.addMergedRegion(new CellRangeAddress(meta.getRowNum(), meta.getRowNum(), 1, 3));
            text(meta, 5, "Refer To", label);
            text(meta, 6, labName, label);
            sheet.addMergedRegion(new CellRangeAddress(meta.getRowNum(), meta.getRowNum(), 6, COLS - 1));
            r++; // spacer

            // Table header
            Row head = sheet.createRow(r++);
            for (int c = 0; c < HEADERS.length; c++) {
                text(head, c, HEADERS[c], header);
            }

            // Data
            int sn = 1;
            for (ShippingOrder o : orders) {
                Row row = sheet.createRow(r++);
                text(row, 0, String.valueOf(sn++), cell);
                text(row, 1, o.getOrderId(), cell);
                text(row, 2, o.getFullName(), cell);
                text(row, 3, o.getAge() == null ? null : String.valueOf(o.getAge()), cell);
                text(row, 4, o.getSex(), cell);
                text(row, 5, o.getTestType(), cell);
                text(row, 6, o.getRequestedDate(), cell);
                text(row, 7, o.getSpecimenCollectedDate(), cell);
            }

            r += 2; // spacer before sign-off
            signOff(sheet, r, orders.size(), label, header, cell);

            sheet.createFreezePane(0, head.getRowNum() + 1);
            wb.write(out);
            return out.toByteArray();
        }
    }

    /** Courier / handover sign-off grid at the foot of the list. */
    private void signOff(XSSFSheet sheet, int startRow, int sampleCount,
                         CellStyle label, CellStyle header, CellStyle cell) {
        Row head = sheet.createRow(startRow);
        text(head, 0, "", header);
        for (int c = 0; c < SIGN_OFF_COLS.length; c++) {
            text(head, c + 1, SIGN_OFF_COLS[c], header);
        }
        for (int i = 0; i < SIGN_OFF_ROLES.length; i++) {
            Row row = sheet.createRow(startRow + 1 + i);
            text(row, 0, SIGN_OFF_ROLES[i], label);
            for (int c = 1; c <= SIGN_OFF_COLS.length; c++) {
                text(row, c, "", cell);
            }
            // Pre-fill the facility's sample count on the sender row; the rest are hand-filled.
            if (i == 0) {
                text(row, 1, String.valueOf(sampleCount), cell);
            }
        }
    }

    private void addCoatOfArms(Workbook wb, XSSFSheet sheet) {
        try (InputStream in = getClass().getResourceAsStream("/img/coa.jpg")) {
            if (in == null) {
                log.warn("Coat of arms image /img/coa.jpg not found on classpath — skipping");
                return;
            }
            int pictureIdx = wb.addPicture(in.readAllBytes(), Workbook.PICTURE_TYPE_JPEG);
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(1);
            anchor.setRow2(3);
            drawing.createPicture(anchor, pictureIdx); // fills the anchor box
        } catch (Exception e) {
            log.warn("Could not embed coat of arms image: {}", e.getMessage());
        }
    }

    // ---- cell / style helpers --------------------------------------------------

    private void titleRow(XSSFSheet sheet, int rowNum, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        text(row, 0, value, style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, COLS - 1));
    }

    private static void text(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        if (value != null) {
            c.setCellValue(value);
        }
        c.setCellStyle(style);
    }

    private CellStyle centered(Workbook wb, int points, boolean bold) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) points);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle bold(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        return s;
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle s = borderedStyle(wb);
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle borderedStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }
}
