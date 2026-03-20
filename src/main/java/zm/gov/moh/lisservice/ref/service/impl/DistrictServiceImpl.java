package zm.gov.moh.lisservice.ref.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.ref.dto.DistrictDTO;
import zm.gov.moh.lisservice.ref.mapper.DistrictMapper;
import zm.gov.moh.lisservice.ref.repository.DistrictRepository;
import zm.gov.moh.lisservice.ref.service.DistrictService;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictMapper districtMapper;
    private final DistrictRepository districtRepository;

    @Override
    public Mono<PagedResponse<DistrictDTO.DistrictResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var content = districtRepository.findAllBy(pageable).map(districtMapper::toResponse);
        var totalCount = districtRepository.count();

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<DistrictDTO.DistrictResponse>builder()
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
    public Mono<PagedResponse<DistrictDTO.DistrictResponse>> findByProvinceId(Short provinceId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var content = districtRepository.findByProvinceId(provinceId, pageable).map(districtMapper::toResponse);
        var totalCount = districtRepository.countByProvinceId(provinceId);

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<DistrictDTO.DistrictResponse>builder()
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
    public Mono<DistrictDTO.DistrictResponse> findById(Long id) {
        return districtRepository.findById(id)
                .map(districtMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("District", String.valueOf(id))));
    }
}
