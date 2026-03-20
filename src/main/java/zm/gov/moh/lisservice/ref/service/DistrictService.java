package zm.gov.moh.lisservice.ref.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.ref.dto.DistrictDTO;

public interface DistrictService {
    Mono<PagedResponse<DistrictDTO.DistrictResponse>> findAll(int page, int size);
    Mono<PagedResponse<DistrictDTO.DistrictResponse>> findByProvinceId(Short provinceId, int page, int size);
    Mono<DistrictDTO.DistrictResponse> findById(Long id);
}
