package moh.gov.zm.lis.lab.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PageMapper;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.exception.ConflictException;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.lab.dto.LaboratoryDTO;
import moh.gov.zm.lis.lab.entity.Laboratory;
import moh.gov.zm.lis.lab.repository.LaboratoryRepository;
import moh.gov.zm.lis.redis.cache.ReferenceSnapshotCache;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LaboratoryService {
    static final String TABLE = "laboratory";

    private final LaboratoryRepository laboratoryRepository;
    private final ReferenceSnapshotCache cache;

    public Mono<PagedResponse<LaboratoryDTO.LaboratoryResponse>> list(
            int page, int size, Long districtId, Short labTypeId, Boolean isActive) {
        return cachedAll().map(all -> {
            List<Laboratory> filtered = all.stream()
                    .filter(l -> districtId == null || districtId.equals(l.getDistrictId()))
                    .filter(l -> labTypeId == null || labTypeId.equals(l.getLabTypeId()))
                    .filter(l -> isActive == null || isActive.equals(l.getIsActive()))
                    .toList();
            return PageMapper.ofList(filtered, page, size, this::toResponse);
        });
    }

    public Mono<LaboratoryDTO.LaboratoryResponse> findById(Short id) {
        return cachedAll()
                .flatMap(list -> list.stream()
                        .filter(l -> id.equals(l.getId()))
                        .findFirst()
                        .map(l -> Mono.just(toResponse(l)))
                        .orElseGet(() -> Mono.error(new ResourceNotFoundException("Laboratory", String.valueOf(id)))));
    }

    public Mono<LaboratoryDTO.LaboratoryResponse> create(LaboratoryDTO.CreateLaboratory request) {
        return laboratoryRepository.existsByLabCode(request.getLabCode())
                .flatMap(exists -> exists
                        ? Mono.error(new ConflictException("Laboratory already exists with code: " + request.getLabCode()))
                        : laboratoryRepository.save(Laboratory.builder()
                        .labCode(request.getLabCode())
                        .labName(request.getLabName())
                        .districtId(request.getDistrictId())
                        .comment(request.getComment())
                        .labTypeId(request.getLabTypeId())
                        .isActive(request.getIsActive() == null || request.getIsActive())
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()))
                .flatMap(this::evictThenReturn)
                .map(this::toResponse);
    }

    public Mono<LaboratoryDTO.LaboratoryResponse> update(Short id, LaboratoryDTO.UpdateLaboratory request) {
        return laboratoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Laboratory", String.valueOf(id))))
                .flatMap(existing -> {
                    if (request.getLabName() != null) {
                        existing.setLabName(request.getLabName());
                    }
                    if (request.getDistrictId() != null) {
                        existing.setDistrictId(request.getDistrictId());
                    }
                    if (request.getComment() != null) {
                        existing.setComment(request.getComment());
                    }
                    if (request.getLabTypeId() != null) {
                        existing.setLabTypeId(request.getLabTypeId());
                    }
                    if (request.getIsActive() != null) {
                        existing.setIsActive(request.getIsActive());
                    }
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return laboratoryRepository.save(existing);
                })
                .flatMap(this::evictThenReturn)
                .map(this::toResponse);
    }

    public Mono<Void> delete(Short id) {
        return laboratoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Laboratory", String.valueOf(id))))
                .flatMap(laboratoryRepository::delete)
                .then(cache.evict(TABLE));
    }

    /** Full laboratory snapshot (Redis-cached); reused by the bulk-upload resolver. */
    public Mono<List<Laboratory>> cachedAll() {
        return cache.snapshot(TABLE, Laboratory.class,
                () -> laboratoryRepository.findAll()
                        .sort(Comparator.comparing(Laboratory::getLabName, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collectList());
    }

    private Mono<Laboratory> evictThenReturn(Laboratory saved) {
        return cache.evict(TABLE).thenReturn(saved);
    }

    private LaboratoryDTO.LaboratoryResponse toResponse(Laboratory e) {
        return LaboratoryDTO.LaboratoryResponse.builder()
                .id(e.getId())
                .labCode(e.getLabCode())
                .labName(e.getLabName())
                .districtId(e.getDistrictId())
                .comment(e.getComment())
                .labTypeId(e.getLabTypeId())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
