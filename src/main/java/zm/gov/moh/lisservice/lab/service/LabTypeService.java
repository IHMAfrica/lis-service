package zm.gov.moh.lisservice.lab.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.LabTypeDTO;

public interface LabTypeService {
    Mono<LabTypeDTO.LabTypeResponse> create(LabTypeDTO.CreateLabType request);
    Mono<PagedResponse<LabTypeDTO.LabTypeResponse>> findAll(int page, int size);
    Mono<LabTypeDTO.LabTypeResponse> findById(Short id);
    Mono<LabTypeDTO.LabTypeResponse> update(Short id, LabTypeDTO.UpdateLabType request);
    Mono<Void> delete(Short id);
}
