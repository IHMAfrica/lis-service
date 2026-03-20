package zm.gov.moh.lisservice.lab.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.LaboratoryDTO;

public interface LaboratoryService {
    Mono<LaboratoryDTO.LaboratoryResponse> create(LaboratoryDTO.CreateLaboratory request);
    Mono<PagedResponse<LaboratoryDTO.LaboratoryResponse>> findAll(int page, int size);
    Mono<LaboratoryDTO.LaboratoryResponse> findById(Short id);
    Mono<LaboratoryDTO.LaboratoryResponse> update(Short id, LaboratoryDTO.UpdateLaboratory request);
    Mono<Void> delete(Short id);
}
