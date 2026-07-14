package moh.gov.zm.lis.ref.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PageMapper;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.redis.cache.ReferenceSnapshotCache;
import moh.gov.zm.lis.ref.dto.DistrictDTO;
import moh.gov.zm.lis.ref.entity.District;
import moh.gov.zm.lis.ref.repository.DistrictRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {
    static final String TABLE = "district";

    private final DistrictRepository districtRepository;
    private final ReferenceSnapshotCache cache;

    public Mono<PagedResponse<DistrictDTO.DistrictResponse>> list(int page, int size, Short provinceId) {
        return snapshot().map(all -> {
            List<District> filtered = all.stream()
                    .filter(d -> provinceId == null || provinceId.equals(d.getProvinceId()))
                    .toList();
            return PageMapper.ofList(filtered, page, size, this::toResponse);
        });
    }

    public Mono<DistrictDTO.DistrictResponse> findById(Long id) {
        return snapshot()
                .flatMap(list -> list.stream()
                        .filter(d -> id.equals(d.getId()))
                        .findFirst()
                        .map(d -> Mono.just(toResponse(d)))
                        .orElseGet(() -> Mono.error(new ResourceNotFoundException("District", String.valueOf(id)))));
    }

    private Mono<List<District>> snapshot() {
        return cache.snapshot(TABLE, District.class,
                () -> districtRepository.findAll()
                        .sort(Comparator.comparing(District::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collectList());
    }

    private DistrictDTO.DistrictResponse toResponse(District d) {
        return DistrictDTO.DistrictResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .provinceId(d.getProvinceId())
                .code(d.getCode())
                .build();
    }
}
