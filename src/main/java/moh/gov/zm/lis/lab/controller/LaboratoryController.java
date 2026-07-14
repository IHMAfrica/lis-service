package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.lab.dto.LaboratoryDTO;
import moh.gov.zm.lis.lab.service.LaboratoryService;
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

@Tag(name = "Laboratories", description = "CRUD for laboratories")
@RestController
@RequestMapping("/api/v1/lis-service/laboratories")
@RequiredArgsConstructor
public class LaboratoryController {
    private final LaboratoryService laboratoryService;

    @Operation(summary = "List laboratories (paged), optionally filtered by district, type and active flag")
    @GetMapping
    public Mono<PagedResponse<LaboratoryDTO.LaboratoryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Short labTypeId,
            @RequestParam(required = false) Boolean isActive) {
        return laboratoryService.list(page, size, districtId, labTypeId, isActive);
    }

    @Operation(summary = "Get a laboratory by id")
    @GetMapping("/{id}")
    public Mono<LaboratoryDTO.LaboratoryResponse> getById(@PathVariable Short id) {
        return laboratoryService.findById(id);
    }

    @Operation(summary = "Create a laboratory")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LaboratoryDTO.LaboratoryResponse> create(@Valid @RequestBody LaboratoryDTO.CreateLaboratory request) {
        return laboratoryService.create(request);
    }

    @Operation(summary = "Update a laboratory")
    @PutMapping("/{id}")
    public Mono<LaboratoryDTO.LaboratoryResponse> update(
            @PathVariable Short id, @Valid @RequestBody LaboratoryDTO.UpdateLaboratory request) {
        return laboratoryService.update(id, request);
    }

    @Operation(summary = "Delete a laboratory")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Short id) {
        return laboratoryService.delete(id);
    }
}
