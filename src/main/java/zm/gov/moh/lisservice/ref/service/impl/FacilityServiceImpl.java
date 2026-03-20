package zm.gov.moh.lisservice.ref.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.ref.dto.FacilityDTO;
import zm.gov.moh.lisservice.ref.mapper.FacilityMapper;
import zm.gov.moh.lisservice.ref.repository.FacilityRepository;
import zm.gov.moh.lisservice.ref.service.FacilityService;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityMapper facilityMapper;
    private final FacilityRepository facilityRepository;

    @Override
    public Mono<PagedResponse<FacilityDTO.FacilityResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var content = facilityRepository.findByIsActiveTrue(pageable).map(facilityMapper::toResponse);
        var totalCount = facilityRepository.countByIsActiveTrue();

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<FacilityDTO.FacilityResponse>builder()
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
    public Mono<PagedResponse<FacilityDTO.FacilityResponse>> findByDistrictId(Long districtId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var content = facilityRepository.findByDistrictIdAndIsActiveTrue(districtId, pageable).map(facilityMapper::toResponse);
        var totalCount = facilityRepository.countByDistrictIdAndIsActiveTrue(districtId);

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<FacilityDTO.FacilityResponse>builder()
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
    public Mono<FacilityDTO.FacilityResponse> findById(Long id) {
        return facilityRepository.findById(id)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(facilityMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Facility", String.valueOf(id))));
    }
}
