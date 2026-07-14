package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.lab.dto.LabTypeDTO;
import moh.gov.zm.lis.lab.service.LabTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Lab types", description = "CRUD for laboratory types")
@RestController
@RequestMapping("/api/v1/lis-service/lab-types")
@RequiredArgsConstructor
public class LabTypeController {
    private final LabTypeService labTypeService;

    @Operation(summary = "List all lab types")
    @GetMapping
    public Flux<LabTypeDTO.LabTypeResponse> list() {
        return labTypeService.findAll();
    }

    @Operation(summary = "Get a lab type by id")
    @GetMapping("/{id}")
    public Mono<LabTypeDTO.LabTypeResponse> getById(@PathVariable Short id) {
        return labTypeService.findById(id);
    }

    @Operation(summary = "Create a lab type")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LabTypeDTO.LabTypeResponse> create(@Valid @RequestBody LabTypeDTO.CreateLabType request) {
        return labTypeService.create(request);
    }

    @Operation(summary = "Update a lab type")
    @PutMapping("/{id}")
    public Mono<LabTypeDTO.LabTypeResponse> update(
            @PathVariable Short id, @Valid @RequestBody LabTypeDTO.UpdateLabType request) {
        return labTypeService.update(id, request);
    }

    @Operation(summary = "Delete a lab type")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Short id) {
        return labTypeService.delete(id);
    }
}
