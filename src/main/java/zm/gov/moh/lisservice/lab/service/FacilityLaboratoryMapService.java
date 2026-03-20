package zm.gov.moh.lisservice.lab.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.FacilityLaboratoryMapDTO;

import java.util.UUID;

public interface FacilityLaboratoryMapService {
    Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> create(FacilityLaboratoryMapDTO.CreateFacilityLaboratoryMap request);
    Mono<PagedResponse<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> findAll(int page, int size);
    Mono<PagedResponse<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> findByFacilityId(Long facilityId, int page, int size);
    Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> findById(UUID id);
    Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> update(UUID id, FacilityLaboratoryMapDTO.UpdateFacilityLaboratoryMap request);
    Mono<Void> delete(UUID id);
}
