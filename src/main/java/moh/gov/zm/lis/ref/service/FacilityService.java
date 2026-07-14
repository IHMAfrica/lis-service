package moh.gov.zm.lis.ref.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PageMapper;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.redis.cache.ReferenceSnapshotCache;
import moh.gov.zm.lis.ref.dto.FacilityDTO;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.repository.FacilityRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {
    static final String TABLE = "facility";

    private final FacilityRepository facilityRepository;
    private final ReferenceSnapshotCache cache;

    public Mono<PagedResponse<FacilityDTO.FacilityResponse>> list(int page, int size, Long districtId, Boolean isActive) {
        return cachedAll().map(all -> {
            List<Facility> filtered = all.stream()
                    .filter(f -> districtId == null || districtId.equals(f.getDistrictId()))
                    .filter(f -> isActive == null || isActive.equals(f.getIsActive()))
                    .toList();
            return PageMapper.ofList(filtered, page, size, this::toResponse);
        });
    }

    public Mono<FacilityDTO.FacilityResponse> findById(Long id) {
        return cachedAll()
                .flatMap(list -> list.stream()
                        .filter(f -> id.equals(f.getId()))
                        .findFirst()
                        .map(f -> Mono.just(toResponse(f)))
                        .orElseGet(() -> Mono.error(new ResourceNotFoundException("Facility", String.valueOf(id)))));
    }

    /** Full facility snapshot (Redis-cached); reused by the bulk-upload resolver. */
    public Mono<List<Facility>> cachedAll() {
        return cache.snapshot(TABLE, Facility.class,
                () -> facilityRepository.findAll()
                        .sort(Comparator.comparing(Facility::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collectList());
    }

    private FacilityDTO.FacilityResponse toResponse(Facility f) {
        return FacilityDTO.FacilityResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .districtId(f.getDistrictId())
                .hmisCode(f.getHmisCode())
                .mflCode(f.getMflCode())
                .isActive(f.getIsActive())
                .build();
    }
}
