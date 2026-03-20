package zm.gov.moh.lisservice.lab.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.lab.dto.TestDTO;
import zm.gov.moh.lisservice.lab.mapper.TestMapper;
import zm.gov.moh.lisservice.lab.repository.TestRepository;
import zm.gov.moh.lisservice.lab.service.TestService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestMapper testMapper;
    private final TestRepository testRepository;

    @Override
    @Transactional
    public Mono<TestDTO.TestResponse> create(TestDTO.CreateTest request) {
        var entity = testMapper.toEntity(request);
        entity.setIsActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return testRepository.save(entity).map(testMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<TestDTO.TestResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var content = testRepository.findByIsActiveTrue(pageable).map(testMapper::toResponse);
        var totalCount = testRepository.countByIsActiveTrue();

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<TestDTO.TestResponse>builder()
                    .content(list)
                    .totalElements(total)
                    .totalPages(totalPages)
                    .currentPage(page)
                    .size(size)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .build();
        });
    }

    @Override
    public Mono<TestDTO.TestResponse> findById(Long id) {
        return testRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(testMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Test", String.valueOf(id))));
    }

    @Override
    @Transactional
    public Mono<TestDTO.TestResponse> update(Long id, TestDTO.UpdateTest request) {
        return testRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Test", String.valueOf(id))))
                .flatMap(entity -> {
                    testMapper.updateEntity(request, entity);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return testRepository.save(entity);
                })
                .map(testMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> delete(Long id) {
        return testRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Test", String.valueOf(id))))
                .flatMap(entity -> {
                    entity.setIsActive(false);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return testRepository.save(entity);
                })
                .then();
    }
}
