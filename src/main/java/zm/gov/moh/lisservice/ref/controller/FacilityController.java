package zm.gov.moh.lisservice.ref.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.ErrorResponse;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.ref.dto.FacilityDTO;
import zm.gov.moh.lisservice.ref.service.FacilityService;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/ref/facilities", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Reference - Facility", description = "Facility reference data API (read-only)")
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(summary = "Get all active facilities (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved facilities",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<PagedResponse<FacilityDTO.FacilityResponse>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return facilityService.findAll(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get all active facilities by district (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved facilities for district",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/district/{districtId}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<PagedResponse<FacilityDTO.FacilityResponse>>> findByDistrictId(
            @PathVariable Long districtId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return facilityService.findByDistrictId(districtId, page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get a facility by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved facility",
                    content = @Content(schema = @Schema(implementation = FacilityDTO.FacilityResponse.class))),
            @ApiResponse(responseCode = "404", description = "Facility not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<FacilityDTO.FacilityResponse>> findById(@PathVariable Long id) {
        return facilityService.findById(id).map(ResponseEntity::ok);
    }
}
