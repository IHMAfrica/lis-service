package zm.gov.moh.lisservice.lab.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.lab.dto.FacilityLaboratoryMapDTO;
import zm.gov.moh.lisservice.lab.mapper.FacilityLaboratoryMapMapper;
import zm.gov.moh.lisservice.lab.repository.FacilityLaboratoryMapRepository;
import zm.gov.moh.lisservice.lab.service.FacilityLaboratoryMapService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacilityLaboratoryMapServiceImpl implements FacilityLaboratoryMapService {

    private final FacilityLaboratoryMapMapper facilityLaboratoryMapMapper;
    private final FacilityLaboratoryMapRepository facilityLaboratoryMapRepository;

    @Override
    @Transactional
    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> create(FacilityLaboratoryMapDTO.CreateFacilityLaboratoryMap request) {
        var entity = facilityLaboratoryMapMapper.toEntity(request);
        entity.setIsActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return facilityLaboratoryMapRepository.save(entity).map(facilityLaboratoryMapMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var content = facilityLaboratoryMapRepository.findByIsActiveTrue(pageable).map(facilityLaboratoryMapMapper::toResponse);
        var totalCount = facilityLaboratoryMapRepository.countByIsActiveTrue();

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>builder()
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
    public Mono<PagedResponse<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> findByFacilityId(Long facilityId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var content = facilityLaboratoryMapRepository.findByFacilityIdAndIsActiveTrue(facilityId, pageable).map(facilityLaboratoryMapMapper::toResponse);
        var totalCount = facilityLaboratoryMapRepository.countByFacilityIdAndIsActiveTrue(facilityId);

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>builder()
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
    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> findById(UUID id) {
        return facilityLaboratoryMapRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(facilityLaboratoryMapMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("FacilityLaboratoryMap", id.toString())));
    }

    @Override
    @Transactional
    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> update(UUID id, FacilityLaboratoryMapDTO.UpdateFacilityLaboratoryMap request) {
        return facilityLaboratoryMapRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("FacilityLaboratoryMap", id.toString())))
                .flatMap(entity -> {
                    facilityLaboratoryMapMapper.updateEntity(request, entity);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return facilityLaboratoryMapRepository.save(entity);
                })
                .map(facilityLaboratoryMapMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> delete(UUID id) {
        return facilityLaboratoryMapRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("FacilityLaboratoryMap", id.toString())))
                .flatMap(entity -> {
                    entity.setIsActive(false);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return facilityLaboratoryMapRepository.save(entity);
                })
                .then();
    }
}
