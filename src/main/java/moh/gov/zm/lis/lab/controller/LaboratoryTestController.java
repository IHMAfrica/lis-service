package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.lab.dto.LaboratoryTestDTO;
import moh.gov.zm.lis.lab.service.LaboratoryTestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Laboratory tests", description = "CRUD for tests offered by a laboratory")
@RestController
@RequestMapping("/api/v1/lis-service/laboratory-tests")
@RequiredArgsConstructor
public class LaboratoryTestController {
    private final LaboratoryTestService laboratoryTestService;

    @Operation(summary = "List laboratory-test offerings (paged), optionally filtered")
    @GetMapping
    public Mono<PagedResponse<LaboratoryTestDTO.LaboratoryTestResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Short laboratoryId,
            @RequestParam(required = false) Long testId,
            @RequestParam(required = false) Boolean isActive) {
        return laboratoryTestService.list(page, size, laboratoryId, testId, isActive);
    }

    @Operation(summary = "Get a laboratory-test offering by id")
    @GetMapping("/{id}")
    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> getById(@PathVariable UUID id) {
        return laboratoryTestService.findById(id);
    }

    @Operation(summary = "Register a test as offered by a laboratory")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> create(
            @Valid @RequestBody LaboratoryTestDTO.CreateLaboratoryTest request) {
        return laboratoryTestService.create(request);
    }

    @Operation(summary = "Update a laboratory-test offering")
    @PutMapping("/{id}")
    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> update(
            @PathVariable UUID id, @Valid @RequestBody LaboratoryTestDTO.UpdateLaboratoryTest request) {
        return laboratoryTestService.update(id, request);
    }

    @Operation(summary = "Delete a laboratory-test offering")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return laboratoryTestService.delete(id);
    }
}
