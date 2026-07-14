package moh.gov.zm.lis.ref.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.ref.dto.DistrictDTO;
import moh.gov.zm.lis.ref.service.DistrictService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Districts", description = "Read access to districts")
@RestController
@RequestMapping("/api/v1/lis-service/districts")
@RequiredArgsConstructor
public class DistrictController {
    private final DistrictService districtService;

    @Operation(summary = "List districts (paged), optionally filtered by province")
    @GetMapping
    public Mono<PagedResponse<DistrictDTO.DistrictResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Short provinceId) {
        return districtService.list(page, size, provinceId);
    }

    @Operation(summary = "Get a district by id")
    @GetMapping("/{id}")
    public Mono<DistrictDTO.DistrictResponse> getById(@PathVariable Long id) {
        return districtService.findById(id);
    }
}
