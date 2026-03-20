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
import zm.gov.moh.lisservice.ref.dto.ProvinceDTO;
import zm.gov.moh.lisservice.ref.service.ProvinceService;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/ref/provinces", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Reference - Province", description = "Province reference data API (read-only)")
public class ProvinceController {
    private final ProvinceService provinceService;

    @Operation(summary = "Get all provinces (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved provinces",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<PagedResponse<ProvinceDTO.ProvinceResponse>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return provinceService.findAll(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get a province by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved province",
                    content = @Content(schema = @Schema(implementation = ProvinceDTO.ProvinceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Province not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<ProvinceDTO.ProvinceResponse>> findById(@PathVariable Short id) {
        return provinceService.findById(id).map(ResponseEntity::ok);
    }
}
