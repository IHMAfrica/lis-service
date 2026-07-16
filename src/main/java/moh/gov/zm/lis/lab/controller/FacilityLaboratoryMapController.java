package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.lab.dto.BulkUploadDTO;
import moh.gov.zm.lis.lab.dto.FacilityLaboratoryMapDTO;
import moh.gov.zm.lis.lab.service.FacilityLaboratoryMapBulkService;
import moh.gov.zm.lis.lab.service.FacilityLaboratoryMapService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Facility-laboratory maps", description = "CRUD and bulk upload for facility-to-lab-test mappings")
@RestController
@RequestMapping("/api/v1/lis-service/facility-laboratory-maps")
@RequiredArgsConstructor
public class FacilityLaboratoryMapController {
    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final FacilityLaboratoryMapService facilityLaboratoryMapService;
    private final FacilityLaboratoryMapBulkService bulkService;

    @Operation(summary = "List facility-to-lab-test mappings (paged), optionally filtered")
    @GetMapping
    public Mono<PagedResponse<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) UUID laboratoryTestId,
            @RequestParam(required = false) Boolean isActive) {
        return facilityLaboratoryMapService.list(page, size, facilityId, laboratoryTestId, isActive);
    }

    @Operation(summary = "Get a facility-to-lab-test mapping by id")
    @GetMapping("/{id}")
    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> getById(@PathVariable UUID id) {
        return facilityLaboratoryMapService.findById(id);
    }

    @Operation(summary = "Lab codes a facility has at least one active mapping to",
            description = "Distinct lab codes reachable from the facility's active facility-to-lab-test mappings.")
    @GetMapping("/lab-codes")
    public Flux<String> labCodes(@RequestParam String mflCode) {
        return facilityLaboratoryMapService.labCodesForFacility(mflCode);
    }

    @Operation(summary = "Create a facility-to-lab-test mapping")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> create(
            @Valid @RequestBody FacilityLaboratoryMapDTO.CreateFacilityLaboratoryMap request) {
        return facilityLaboratoryMapService.create(request);
    }

    @Operation(summary = "Update a facility-to-lab-test mapping")
    @PutMapping("/{id}")
    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> update(
            @PathVariable UUID id, @Valid @RequestBody FacilityLaboratoryMapDTO.UpdateFacilityLaboratoryMap request) {
        return facilityLaboratoryMapService.update(id, request);
    }

    @Operation(summary = "Delete a facility-to-lab-test mapping")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return facilityLaboratoryMapService.delete(id);
    }

    @Operation(summary = "Download the bulk-upload Excel template",
            description = "Returns an .xlsx with dropdowns of the current mfl, loinc and lab codes.")
    @GetMapping("/template")
    public Mono<ResponseEntity<byte[]>> downloadTemplate() {
        return bulkService.generateTemplate()
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"facility-lab-test-mapping-template.xlsx\"")
                        .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
                        .body(bytes));
    }

    @Operation(summary = "Bulk upload facility-to-lab-test mappings from a filled-in template",
            description = "Existing active mappings are left unchanged, inactive ones are reactivated, "
                    + "and new combinations are created. Returns a summary of what happened.")
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<BulkUploadDTO.BulkUploadSummary> bulkUpload(@RequestPart("file") FilePart file) {
        return DataBufferUtils.join(file.content())
                .map(FacilityLaboratoryMapController::toByteArray)
                .flatMap(bulkService::process);
    }

    private static byte[] toByteArray(DataBuffer buffer) {
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer);
        return bytes;
    }
}
