package moh.gov.zm.lis.ref.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.redis.cache.ReferenceSnapshotCache;
import moh.gov.zm.lis.ref.dto.ProvinceDTO;
import moh.gov.zm.lis.ref.entity.Province;
import moh.gov.zm.lis.ref.repository.ProvinceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvinceService {
    static final String TABLE = "province";

    private final ProvinceRepository provinceRepository;
    private final ReferenceSnapshotCache cache;

    public Flux<ProvinceDTO.ProvinceResponse> findAll() {
        return snapshot()
                .flatMapMany(Flux::fromIterable)
                .map(this::toResponse);
    }

    public Mono<ProvinceDTO.ProvinceResponse> findById(Short id) {
        return snapshot()
                .flatMap(list -> list.stream()
                        .filter(p -> id.equals(p.getId()))
                        .findFirst()
                        .map(p -> Mono.just(toResponse(p)))
                        .orElseGet(() -> Mono.error(new ResourceNotFoundException("Province", String.valueOf(id)))));
    }

    private Mono<List<Province>> snapshot() {
        return cache.snapshot(TABLE, Province.class,
                () -> provinceRepository.findAll()
                        .sort(Comparator.comparing(Province::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collectList());
    }

    private ProvinceDTO.ProvinceResponse toResponse(Province p) {
        return ProvinceDTO.ProvinceResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .build();
    }
}
