package zm.gov.moh.lisservice.ref.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.ref.dto.FacilityDTO;

public interface FacilityService {
    Mono<PagedResponse<FacilityDTO.FacilityResponse>> findAll(int page, int size);
    Mono<PagedResponse<FacilityDTO.FacilityResponse>> findByDistrictId(Long districtId, int page, int size);
    Mono<FacilityDTO.FacilityResponse> findById(Long id);
}
