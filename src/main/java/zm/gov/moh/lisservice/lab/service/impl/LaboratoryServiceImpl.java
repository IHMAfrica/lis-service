package zm.gov.moh.lisservice.lab.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.exception.ValidationException;
import zm.gov.moh.lisservice.lab.dto.LaboratoryDTO;
import zm.gov.moh.lisservice.lab.mapper.LaboratoryMapper;
import zm.gov.moh.lisservice.lab.repository.LaboratoryRepository;
import zm.gov.moh.lisservice.lab.service.LaboratoryService;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl implements LaboratoryService {

    private final LaboratoryMapper laboratoryMapper;
    private final LaboratoryRepository laboratoryRepository;

    @Override
    @Transactional
    public Mono<LaboratoryDTO.LaboratoryResponse> create(LaboratoryDTO.CreateLaboratory request) {
        return laboratoryRepository.existsByLabCodeAndIsActiveTrue(request.getLabCode())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ValidationException("Lab code already exists",
                                Map.of("labCode", "Lab code already exists: " + request.getLabCode())));
                    }
                    var entity = laboratoryMapper.toEntity(request);
                    entity.setIsActive(true);
                    entity.setCreatedAt(LocalDateTime.now());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return laboratoryRepository.save(entity);
                })
                .map(laboratoryMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<LaboratoryDTO.LaboratoryResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "labName"));
        var content = laboratoryRepository.findByIsActiveTrue(pageable).map(laboratoryMapper::toResponse);
        var totalCount = laboratoryRepository.countByIsActiveTrue();

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<LaboratoryDTO.LaboratoryResponse>builder()
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
    public Mono<LaboratoryDTO.LaboratoryResponse> findById(Short id) {
        return laboratoryRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(laboratoryMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Laboratory", String.valueOf(id))));
    }

    @Override
    @Transactional
    public Mono<LaboratoryDTO.LaboratoryResponse> update(Short id, LaboratoryDTO.UpdateLaboratory request) {
        return laboratoryRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Laboratory", String.valueOf(id))))
                .flatMap(entity -> {
                    laboratoryMapper.updateEntity(request, entity);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return laboratoryRepository.save(entity);
                })
                .map(laboratoryMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> delete(Short id) {
        return laboratoryRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Laboratory", String.valueOf(id))))
                .flatMap(entity -> {
                    entity.setIsActive(false);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return laboratoryRepository.save(entity);
                })
                .then();
    }
}
