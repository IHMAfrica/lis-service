package moh.gov.zm.lis.lab.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PageMapper;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.exception.ConflictException;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.lab.dto.LaboratoryTestDTO;
import moh.gov.zm.lis.lab.entity.LaboratoryTest;
import moh.gov.zm.lis.lab.repository.LaboratoryTestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LaboratoryTestService {
    private final LaboratoryTestRepository laboratoryTestRepository;
    private final R2dbcEntityTemplate template;

    public Mono<PagedResponse<LaboratoryTestDTO.LaboratoryTestResponse>> list(
            int page, int size, Short laboratoryId, Long testId, Boolean isActive) {
        List<Criteria> parts = new ArrayList<>();
        if (laboratoryId != null) {
            parts.add(Criteria.where("laboratoryId").is(laboratoryId));
        }
        if (testId != null) {
            parts.add(Criteria.where("testId").is(testId));
        }
        if (isActive != null) {
            parts.add(Criteria.where("isActive").is(isActive));
        }
        Query base = parts.isEmpty() ? Query.empty() : Query.query(Criteria.from(parts));
        Query paged = base.with(PageRequest.of(page, size, Sort.by("createdAt")));

        return template.select(paged, LaboratoryTest.class)
                .map(this::toResponse)
                .collectList()
                .zipWith(template.count(base, LaboratoryTest.class))
                .map(t -> PageMapper.of(t.getT1(), t.getT2(), page, size));
    }

    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> findById(UUID id) {
        return load(id).map(this::toResponse);
    }

    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> create(LaboratoryTestDTO.CreateLaboratoryTest request) {
        return laboratoryTestRepository.existsByLaboratoryIdAndTestId(request.getLaboratoryId(), request.getTestId())
                .flatMap(exists -> exists
                        ? Mono.error(new ConflictException(
                        "Test %d is already offered by laboratory %d".formatted(request.getTestId(), request.getLaboratoryId())))
                        : laboratoryTestRepository.save(LaboratoryTest.builder()
                        .laboratoryId(request.getLaboratoryId())
                        .testId(request.getTestId())
                        .isActive(request.getIsActive() == null || request.getIsActive())
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()))
                .map(this::toResponse);
    }

    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> update(UUID id, LaboratoryTestDTO.UpdateLaboratoryTest request) {
        return load(id)
                .flatMap(existing -> {
                    if (request.getIsActive() != null) {
                        existing.setIsActive(request.getIsActive());
                    }
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return laboratoryTestRepository.save(existing);
                })
                .map(this::toResponse);
    }

    public Mono<Void> delete(UUID id) {
        return load(id).flatMap(laboratoryTestRepository::delete);
    }

    private Mono<LaboratoryTest> load(UUID id) {
        return laboratoryTestRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LaboratoryTest", String.valueOf(id))));
    }

    private LaboratoryTestDTO.LaboratoryTestResponse toResponse(LaboratoryTest e) {
        return LaboratoryTestDTO.LaboratoryTestResponse.builder()
                .id(e.getId())
                .laboratoryId(e.getLaboratoryId())
                .testId(e.getTestId())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
