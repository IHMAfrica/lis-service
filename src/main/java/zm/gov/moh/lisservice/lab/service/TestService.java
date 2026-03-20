package zm.gov.moh.lisservice.lab.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.TestDTO;

public interface TestService {
    Mono<TestDTO.TestResponse> create(TestDTO.CreateTest request);
    Mono<PagedResponse<TestDTO.TestResponse>> findAll(int page, int size);
    Mono<TestDTO.TestResponse> findById(Long id);
    Mono<TestDTO.TestResponse> update(Long id, TestDTO.UpdateTest request);
    Mono<Void> delete(Long id);
}
