package zm.gov.moh.lisservice.lab.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.lab.dto.LabTypeDTO;
import zm.gov.moh.lisservice.lab.mapper.LabTypeMapper;
import zm.gov.moh.lisservice.lab.repository.LabTypeRepository;
import zm.gov.moh.lisservice.lab.service.LabTypeService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LabTypeServiceImpl implements LabTypeService {

    private final LabTypeMapper labTypeMapper;
    private final LabTypeRepository labTypeRepository;

    @Override
    @Transactional
    public Mono<LabTypeDTO.LabTypeResponse> create(LabTypeDTO.CreateLabType request) {
        var entity = labTypeMapper.toEntity(request);
        entity.setIsActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return labTypeRepository.save(entity).map(labTypeMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<LabTypeDTO.LabTypeResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var content = labTypeRepository.findByIsActiveTrue(pageable).map(labTypeMapper::toResponse);
        var totalCount = labTypeRepository.countByIsActiveTrue();

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<LabTypeDTO.LabTypeResponse>builder()
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
    public Mono<LabTypeDTO.LabTypeResponse> findById(Short id) {
        return labTypeRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(labTypeMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LabType", String.valueOf(id))));
    }

    @Override
    @Transactional
    public Mono<LabTypeDTO.LabTypeResponse> update(Short id, LabTypeDTO.UpdateLabType request) {
        return labTypeRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LabType", String.valueOf(id))))
                .flatMap(entity -> {
                    labTypeMapper.updateEntity(request, entity);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return labTypeRepository.save(entity);
                })
                .map(labTypeMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> delete(Short id) {
        return labTypeRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LabType", String.valueOf(id))))
                .flatMap(entity -> {
                    entity.setIsActive(false);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return labTypeRepository.save(entity);
                })
                .then();
    }
}
