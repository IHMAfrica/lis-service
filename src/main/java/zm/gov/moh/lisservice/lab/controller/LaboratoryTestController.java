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
import zm.gov.moh.lisservice.lab.dto.LaboratoryTestDTO;
import zm.gov.moh.lisservice.lab.service.LaboratoryTestService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/laboratory-tests", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Laboratory Test", description = "Laboratory-test assignment management API")
public class LaboratoryTestController {

    private final LaboratoryTestService laboratoryTestService;

    @Operation(summary = "Assign a test to a laboratory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Laboratory test assigned successfully",
                    content = @Content(schema = @Schema(implementation = LaboratoryTestDTO.LaboratoryTestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<LaboratoryTestDTO.LaboratoryTestResponse>> create(
            @Valid @RequestBody LaboratoryTestDTO.CreateLaboratoryTest request
    ) {
        return laboratoryTestService.create(request)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @Operation(summary = "Get all laboratory-test assignments (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved laboratory tests",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<PagedResponse<LaboratoryTestDTO.LaboratoryTestResponse>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return laboratoryTestService.findAll(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get all tests assigned to a laboratory (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tests for laboratory",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/laboratory/{laboratoryId}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<PagedResponse<LaboratoryTestDTO.LaboratoryTestResponse>>> findByLaboratoryId(
            @PathVariable Short laboratoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return laboratoryTestService.findByLaboratoryId(laboratoryId, page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get a laboratory-test assignment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved laboratory test",
                    content = @Content(schema = @Schema(implementation = LaboratoryTestDTO.LaboratoryTestResponse.class))),
            @ApiResponse(responseCode = "404", description = "Laboratory test not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<LaboratoryTestDTO.LaboratoryTestResponse>> findById(@PathVariable UUID id) {
        return laboratoryTestService.findById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Update a laboratory-test assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Laboratory test updated successfully",
                    content = @Content(schema = @Schema(implementation = LaboratoryTestDTO.LaboratoryTestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Laboratory test not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<LaboratoryTestDTO.LaboratoryTestResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LaboratoryTestDTO.UpdateLaboratoryTest request
    ) {
        return laboratoryTestService.update(id, request).map(ResponseEntity::ok);
    }

    @Operation(summary = "Deactivate a laboratory-test assignment (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Laboratory test deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Laboratory test not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return laboratoryTestService.delete(id).then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
