package zm.gov.moh.lisservice.ref.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.ref.dto.ProvinceDTO;

public interface ProvinceService {
    Mono<PagedResponse<ProvinceDTO.ProvinceResponse>> findAll(int page, int size);
    Mono<ProvinceDTO.ProvinceResponse> findById(Short id);
}
