package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.lab.dto.TestDTO;
import moh.gov.zm.lis.lab.service.TestService;
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

@Tag(name = "Tests", description = "CRUD for the test catalogue")
@RestController
@RequestMapping("/api/v1/lis-service/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @Operation(summary = "List tests (paged), optionally filtered by active flag")
    @GetMapping
    public Mono<PagedResponse<TestDTO.TestResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isActive) {
        return testService.list(page, size, isActive);
    }

    @Operation(summary = "Get a test by id")
    @GetMapping("/{id}")
    public Mono<TestDTO.TestResponse> getById(@PathVariable Long id) {
        return testService.findById(id);
    }

    @Operation(summary = "Create a test")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TestDTO.TestResponse> create(@Valid @RequestBody TestDTO.CreateTest request) {
        return testService.create(request);
    }

    @Operation(summary = "Update a test")
    @PutMapping("/{id}")
    public Mono<TestDTO.TestResponse> update(
            @PathVariable Long id, @Valid @RequestBody TestDTO.UpdateTest request) {
        return testService.update(id, request);
    }

    @Operation(summary = "Delete a test")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return testService.delete(id);
    }
}
