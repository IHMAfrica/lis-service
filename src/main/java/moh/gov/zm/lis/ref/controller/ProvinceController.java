package moh.gov.zm.lis.ref.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.ref.dto.ProvinceDTO;
import moh.gov.zm.lis.ref.service.ProvinceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Provinces", description = "Read access to provinces")
@RestController
@RequestMapping("/api/v1/lis-service/provinces")
@RequiredArgsConstructor
public class ProvinceController {
    private final ProvinceService provinceService;

    @Operation(summary = "List all provinces")
    @GetMapping
    public Flux<ProvinceDTO.ProvinceResponse> list() {
        return provinceService.findAll();
    }

    @Operation(summary = "Get a province by id")
    @GetMapping("/{id}")
    public Mono<ProvinceDTO.ProvinceResponse> getById(@PathVariable Short id) {
        return provinceService.findById(id);
    }
}
