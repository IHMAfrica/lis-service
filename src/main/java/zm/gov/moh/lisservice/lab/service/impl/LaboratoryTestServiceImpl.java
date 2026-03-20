package zm.gov.moh.lisservice.lab.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.lab.dto.LaboratoryTestDTO;
import zm.gov.moh.lisservice.lab.mapper.LaboratoryTestMapper;
import zm.gov.moh.lisservice.lab.repository.LaboratoryTestRepository;
import zm.gov.moh.lisservice.lab.service.LaboratoryTestService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LaboratoryTestServiceImpl implements LaboratoryTestService {

    private final LaboratoryTestMapper laboratoryTestMapper;
    private final LaboratoryTestRepository laboratoryTestRepository;

    @Override
    @Transactional
    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> create(LaboratoryTestDTO.CreateLaboratoryTest request) {
        var entity = laboratoryTestMapper.toEntity(request);
        entity.setIsActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return laboratoryTestRepository.save(entity).map(laboratoryTestMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<LaboratoryTestDTO.LaboratoryTestResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var content = laboratoryTestRepository.findByIsActiveTrue(pageable).map(laboratoryTestMapper::toResponse);
        var totalCount = laboratoryTestRepository.countByIsActiveTrue();

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<LaboratoryTestDTO.LaboratoryTestResponse>builder()
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
    public Mono<PagedResponse<LaboratoryTestDTO.LaboratoryTestResponse>> findByLaboratoryId(Short laboratoryId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var content = laboratoryTestRepository.findByLaboratoryIdAndIsActiveTrue(laboratoryId, pageable).map(laboratoryTestMapper::toResponse);
        var totalCount = laboratoryTestRepository.countByLaboratoryIdAndIsActiveTrue(laboratoryId);

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<LaboratoryTestDTO.LaboratoryTestResponse>builder()
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
    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> findById(UUID id) {
        return laboratoryTestRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(laboratoryTestMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LaboratoryTest", id.toString())));
    }

    @Override
    @Transactional
    public Mono<LaboratoryTestDTO.LaboratoryTestResponse> update(UUID id, LaboratoryTestDTO.UpdateLaboratoryTest request) {
        return laboratoryTestRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LaboratoryTest", id.toString())))
                .flatMap(entity -> {
                    laboratoryTestMapper.updateEntity(request, entity);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return laboratoryTestRepository.save(entity);
                })
                .map(laboratoryTestMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> delete(UUID id) {
        return laboratoryTestRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LaboratoryTest", id.toString())))
                .flatMap(entity -> {
                    entity.setIsActive(false);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return laboratoryTestRepository.save(entity);
                })
                .then();
    }
}
