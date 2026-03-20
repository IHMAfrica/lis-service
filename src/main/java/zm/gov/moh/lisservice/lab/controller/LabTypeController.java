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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.ErrorResponse;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.LabTypeDTO;
import zm.gov.moh.lisservice.lab.service.LabTypeService;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/lab-types", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Lab Type", description = "Lab Type management API")
public class LabTypeController {

    private final LabTypeService labTypeService;

    @Operation(summary = "Create a lab type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lab type created successfully",
                    content = @Content(schema = @Schema(implementation = LabTypeDTO.LabTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<LabTypeDTO.LabTypeResponse>> create(@Valid @RequestBody LabTypeDTO.CreateLabType request) {
        return labTypeService.create(request)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @Operation(summary = "Get all lab types (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved lab types",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<PagedResponse<LabTypeDTO.LabTypeResponse>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return labTypeService.findAll(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get a lab type by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved lab type",
                    content = @Content(schema = @Schema(implementation = LabTypeDTO.LabTypeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lab type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<LabTypeDTO.LabTypeResponse>> findById(@PathVariable Short id) {
        return labTypeService.findById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Update a lab type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lab type updated successfully",
                    content = @Content(schema = @Schema(implementation = LabTypeDTO.LabTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lab type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<LabTypeDTO.LabTypeResponse>> update(
            @PathVariable Short id,
            @Valid @RequestBody LabTypeDTO.UpdateLabType request
    ) {
        return labTypeService.update(id, request).map(ResponseEntity::ok);
    }

    @Operation(summary = "Deactivate a lab type (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lab type deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Lab type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Short id) {
        return labTypeService.delete(id).then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
