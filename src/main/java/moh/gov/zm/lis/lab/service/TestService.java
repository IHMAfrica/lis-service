package moh.gov.zm.lis.lab.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PageMapper;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.exception.ConflictException;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.lab.dto.TestDTO;
import moh.gov.zm.lis.lab.entity.Test;
import moh.gov.zm.lis.lab.repository.TestRepository;
import moh.gov.zm.lis.redis.cache.ReferenceSnapshotCache;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestService {
    static final String TABLE = "test";

    private final TestRepository testRepository;
    private final ReferenceSnapshotCache cache;

    public Mono<PagedResponse<TestDTO.TestResponse>> list(int page, int size, Boolean isActive) {
        return cachedAll().map(all -> {
            List<Test> filtered = all.stream()
                    .filter(t -> isActive == null || isActive.equals(t.getIsActive()))
                    .toList();
            return PageMapper.ofList(filtered, page, size, this::toResponse);
        });
    }

    public Mono<TestDTO.TestResponse> findById(Long id) {
        return cachedAll()
                .flatMap(list -> list.stream()
                        .filter(t -> id.equals(t.getId()))
                        .findFirst()
                        .map(t -> Mono.just(toResponse(t)))
                        .orElseGet(() -> Mono.error(new ResourceNotFoundException("Test", String.valueOf(id)))));
    }

    public Mono<TestDTO.TestResponse> create(TestDTO.CreateTest request) {
        return testRepository.existsByLoincCode(request.getLoincCode())
                .flatMap(exists -> exists
                        ? Mono.error(new ConflictException("Test already exists with LOINC code: " + request.getLoincCode()))
                        : testRepository.save(Test.builder()
                        .name(request.getName())
                        .loincCode(request.getLoincCode())
                        .abbreviation(request.getAbbreviation())
                        .shortTitle(request.getShortTitle())
                        .isCompositeTest(request.getIsCompositeTest() != null && request.getIsCompositeTest())
                        .isActive(request.getIsActive() == null || request.getIsActive())
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()))
                .flatMap(this::evictThenReturn)
                .map(this::toResponse);
    }

    public Mono<TestDTO.TestResponse> update(Long id, TestDTO.UpdateTest request) {
        return testRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Test", String.valueOf(id))))
                .flatMap(existing -> {
                    if (request.getName() != null) {
                        existing.setName(request.getName());
                    }
                    if (request.getAbbreviation() != null) {
                        existing.setAbbreviation(request.getAbbreviation());
                    }
                    if (request.getShortTitle() != null) {
                        existing.setShortTitle(request.getShortTitle());
                    }
                    if (request.getIsCompositeTest() != null) {
                        existing.setIsCompositeTest(request.getIsCompositeTest());
                    }
                    if (request.getIsActive() != null) {
                        existing.setIsActive(request.getIsActive());
                    }
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return testRepository.save(existing);
                })
                .flatMap(this::evictThenReturn)
                .map(this::toResponse);
    }

    public Mono<Void> delete(Long id) {
        return testRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Test", String.valueOf(id))))
                .flatMap(testRepository::delete)
                .then(cache.evict(TABLE));
    }

    /** Full test snapshot (Redis-cached); reused by the bulk-upload resolver. */
    public Mono<List<Test>> cachedAll() {
        return cache.snapshot(TABLE, Test.class,
                () -> testRepository.findAll()
                        .sort(Comparator.comparing(Test::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collectList());
    }

    private Mono<Test> evictThenReturn(Test saved) {
        return cache.evict(TABLE).thenReturn(saved);
    }

    private TestDTO.TestResponse toResponse(Test e) {
        return TestDTO.TestResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .loincCode(e.getLoincCode())
                .abbreviation(e.getAbbreviation())
                .shortTitle(e.getShortTitle())
                .isCompositeTest(e.getIsCompositeTest())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
