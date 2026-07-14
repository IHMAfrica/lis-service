package moh.gov.zm.lis.lab.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.exception.ConflictException;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.lab.dto.LabTypeDTO;
import moh.gov.zm.lis.lab.entity.LabType;
import moh.gov.zm.lis.lab.repository.LabTypeRepository;
import moh.gov.zm.lis.redis.cache.ReferenceSnapshotCache;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabTypeService {
    static final String TABLE = "lab_type";

    private final LabTypeRepository labTypeRepository;
    private final ReferenceSnapshotCache cache;

    public Flux<LabTypeDTO.LabTypeResponse> findAll() {
        return snapshot().flatMapMany(Flux::fromIterable).map(this::toResponse);
    }

    public Mono<LabTypeDTO.LabTypeResponse> findById(Short id) {
        return snapshot()
                .flatMap(list -> list.stream()
                        .filter(e -> id.equals(e.getId()))
                        .findFirst()
                        .map(e -> Mono.just(toResponse(e)))
                        .orElseGet(() -> Mono.error(new ResourceNotFoundException("LabType", String.valueOf(id)))));
    }

    public Mono<LabTypeDTO.LabTypeResponse> create(LabTypeDTO.CreateLabType request) {
        return labTypeRepository.existsByName(request.getName())
                .flatMap(exists -> exists
                        ? Mono.error(new ConflictException("Lab type already exists with name: " + request.getName()))
                        : labTypeRepository.save(LabType.builder()
                        .name(request.getName())
                        .isActive(request.getIsActive() == null || request.getIsActive())
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()))
                .flatMap(this::evictThenReturn)
                .map(this::toResponse);
    }

    public Mono<LabTypeDTO.LabTypeResponse> update(Short id, LabTypeDTO.UpdateLabType request) {
        return labTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LabType", String.valueOf(id))))
                .flatMap(existing -> ensureNameAvailable(existing, request.getName())
                        .then(Mono.defer(() -> {
                            if (request.getName() != null) {
                                existing.setName(request.getName());
                            }
                            if (request.getIsActive() != null) {
                                existing.setIsActive(request.getIsActive());
                            }
                            existing.setUpdatedAt(OffsetDateTime.now());
                            return labTypeRepository.save(existing);
                        })))
                .flatMap(this::evictThenReturn)
                .map(this::toResponse);
    }

    public Mono<Void> delete(Short id) {
        return labTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LabType", String.valueOf(id))))
                .flatMap(labTypeRepository::delete)
                .then(cache.evict(TABLE));
    }

    private Mono<List<LabType>> snapshot() {
        return cache.snapshot(TABLE, LabType.class,
                () -> labTypeRepository.findAll()
                        .sort(Comparator.comparing(LabType::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collectList());
    }

    private Mono<LabType> evictThenReturn(LabType saved) {
        return cache.evict(TABLE).thenReturn(saved);
    }

    private Mono<Void> ensureNameAvailable(LabType existing, String newName) {
        if (newName == null || newName.equals(existing.getName())) {
            return Mono.empty();
        }
        return labTypeRepository.existsByName(newName)
                .flatMap(exists -> exists
                        ? Mono.error(new ConflictException("Lab type already exists with name: " + newName))
                        : Mono.empty());
    }

    private LabTypeDTO.LabTypeResponse toResponse(LabType e) {
        return LabTypeDTO.LabTypeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
