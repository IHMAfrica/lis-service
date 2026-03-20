package zm.gov.moh.lisservice.ref.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.ref.dto.ProvinceDTO;
import zm.gov.moh.lisservice.ref.mapper.ProvinceMapper;
import zm.gov.moh.lisservice.ref.repository.ProvinceRepository;
import zm.gov.moh.lisservice.ref.service.ProvinceService;

@Service
@RequiredArgsConstructor
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceMapper provinceMapper;
    private final ProvinceRepository provinceRepository;

    @Override
    public Mono<PagedResponse<ProvinceDTO.ProvinceResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var content = provinceRepository.findAllBy(pageable).map(provinceMapper::toResponse);
        var totalCount = provinceRepository.count();

        return Mono.zip(content.collectList(), totalCount).map(tuple -> {
            var list = tuple.getT1();
            var total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);
            return PagedResponse.<ProvinceDTO.ProvinceResponse>builder()
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
    public Mono<ProvinceDTO.ProvinceResponse> findById(Short id) {
        return provinceRepository.findById(id)
                .map(provinceMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Province", String.valueOf(id))));
    }
}
