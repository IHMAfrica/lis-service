package zm.gov.moh.lisservice.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.ErrorResponse;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.FacilityLaboratoryMapDTO;
import zm.gov.moh.lisservice.lab.service.FacilityLaboratoryMapService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/facility-laboratory-maps", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Facility Laboratory Map", description = "Facility to laboratory-test mapping management API")
public class FacilityLaboratoryMapController {

    private final FacilityLaboratoryMapService facilityLaboratoryMapService;

    @Operation(summary = "Create a facility-laboratory mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mapping created successfully",
                    content = @Content(schema = @Schema(implementation = FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public Mono<ResponseEntity<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> create(
            @Valid @RequestBody FacilityLaboratoryMapDTO.CreateFacilityLaboratoryMap request
    ) {
        return facilityLaboratoryMapService.create(request)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @Operation(summary = "Get all facility-laboratory mappings (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved mappings",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public Mono<ResponseEntity<PagedResponse<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return facilityLaboratoryMapService.findAll(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get all laboratory mappings for a facility (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved mappings for facility",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/facility/{facilityId}")
    public Mono<ResponseEntity<PagedResponse<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>>> findByFacilityId(
            @PathVariable Long facilityId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return facilityLaboratoryMapService.findByFacilityId(facilityId, page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get a facility-laboratory mapping by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved mapping",
                    content = @Content(schema = @Schema(implementation = FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mapping not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> findById(@PathVariable UUID id) {
        return facilityLaboratoryMapService.findById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Update a facility-laboratory mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping updated successfully",
                    content = @Content(schema = @Schema(implementation = FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mapping not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FacilityLaboratoryMapDTO.UpdateFacilityLaboratoryMap request
    ) {
        return facilityLaboratoryMapService.update(id, request).map(ResponseEntity::ok);
    }

    @Operation(summary = "Deactivate a facility-laboratory mapping (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mapping deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Mapping not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return facilityLaboratoryMapService.delete(id).then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
