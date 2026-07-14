package moh.gov.zm.lis.ref.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.ref.dto.FacilityDTO;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Facilities", description = "Read access to health facilities")
@RestController
@RequestMapping("/api/v1/lis-service/facilities")
@RequiredArgsConstructor
public class FacilityController {
    private final FacilityService facilityService;

    @Operation(summary = "List facilities (paged), optionally filtered by district and active flag")
    @GetMapping
    public Mono<PagedResponse<FacilityDTO.FacilityResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Boolean isActive) {
        return facilityService.list(page, size, districtId, isActive);
    }

    @Operation(summary = "Get a facility by id")
    @GetMapping("/{id}")
    public Mono<FacilityDTO.FacilityResponse> getById(@PathVariable Long id) {
        return facilityService.findById(id);
    }
}
