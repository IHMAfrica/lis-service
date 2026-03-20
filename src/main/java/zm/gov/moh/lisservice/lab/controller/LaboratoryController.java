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
import zm.gov.moh.lisservice.lab.dto.LaboratoryDTO;
import zm.gov.moh.lisservice.lab.service.LaboratoryService;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/laboratories", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Laboratory", description = "Laboratory management API")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    @Operation(summary = "Create a laboratory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Laboratory created successfully",
                    content = @Content(schema = @Schema(implementation = LaboratoryDTO.LaboratoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or lab code already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<LaboratoryDTO.LaboratoryResponse>> create(@Valid @RequestBody LaboratoryDTO.CreateLaboratory request) {
        return laboratoryService.create(request)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @Operation(summary = "Get all laboratories (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved laboratories",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<PagedResponse<LaboratoryDTO.LaboratoryResponse>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return laboratoryService.findAll(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get a laboratory by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved laboratory",
                    content = @Content(schema = @Schema(implementation = LaboratoryDTO.LaboratoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Laboratory not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<LaboratoryDTO.LaboratoryResponse>> findById(@PathVariable Short id) {
        return laboratoryService.findById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Update a laboratory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Laboratory updated successfully",
                    content = @Content(schema = @Schema(implementation = LaboratoryDTO.LaboratoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Laboratory not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<LaboratoryDTO.LaboratoryResponse>> update(
            @PathVariable Short id,
            @Valid @RequestBody LaboratoryDTO.UpdateLaboratory request
    ) {
        return laboratoryService.update(id, request).map(ResponseEntity::ok);
    }

    @Operation(summary = "Deactivate a laboratory (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Laboratory deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Laboratory not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Short id) {
        return laboratoryService.delete(id).then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
