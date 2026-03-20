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
import zm.gov.moh.lisservice.lab.dto.TestDTO;
import zm.gov.moh.lisservice.lab.service.TestService;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/tests", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Test", description = "Laboratory test catalogue management API")
public class TestController {

    private final TestService testService;

    @Operation(summary = "Create a test")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Test created successfully",
                    content = @Content(schema = @Schema(implementation = TestDTO.TestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<TestDTO.TestResponse>> create(@Valid @RequestBody TestDTO.CreateTest request) {
        return testService.create(request)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @Operation(summary = "Get all tests (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tests",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<PagedResponse<TestDTO.TestResponse>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return testService.findAll(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get a test by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved test",
                    content = @Content(schema = @Schema(implementation = TestDTO.TestResponse.class))),
            @ApiResponse(responseCode = "404", description = "Test not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN', 'ROLE_HIE_MANAGER_USER')")
    public Mono<ResponseEntity<TestDTO.TestResponse>> findById(@PathVariable Long id) {
        return testService.findById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Update a test")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test updated successfully",
                    content = @Content(schema = @Schema(implementation = TestDTO.TestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Test not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    public Mono<ResponseEntity<TestDTO.TestResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TestDTO.UpdateTest request
    ) {
        return testService.update(id, request).map(ResponseEntity::ok);
    }

    @Operation(summary = "Deactivate a test (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Test deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Test not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_HIE_MANAGER_ADMIN')")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return testService.delete(id).then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
