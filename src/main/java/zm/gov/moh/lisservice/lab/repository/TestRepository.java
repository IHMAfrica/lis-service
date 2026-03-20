package zm.gov.moh.lisservice.lab.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.lab.entity.Test;

@Repository
public interface TestRepository extends R2dbcRepository<Test, Long> {
    Flux<Test> findByIsActiveTrue(Pageable pageable);
    Mono<Long> countByIsActiveTrue();
    Mono<Test> findFirstByNameIgnoreCaseAndIsActiveTrue(String name);
}
