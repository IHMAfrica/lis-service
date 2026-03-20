package zm.gov.moh.lisservice.lab.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.LaboratoryTestDTO;

import java.util.UUID;

public interface LaboratoryTestService {
    Mono<LaboratoryTestDTO.LaboratoryTestResponse> create(LaboratoryTestDTO.CreateLaboratoryTest request);
    Mono<PagedResponse<LaboratoryTestDTO.LaboratoryTestResponse>> findAll(int page, int size);
    Mono<PagedResponse<LaboratoryTestDTO.LaboratoryTestResponse>> findByLaboratoryId(Short laboratoryId, int page, int size);
    Mono<LaboratoryTestDTO.LaboratoryTestResponse> findById(UUID id);
    Mono<LaboratoryTestDTO.LaboratoryTestResponse> update(UUID id, LaboratoryTestDTO.UpdateLaboratoryTest request);
    Mono<Void> delete(UUID id);
}
