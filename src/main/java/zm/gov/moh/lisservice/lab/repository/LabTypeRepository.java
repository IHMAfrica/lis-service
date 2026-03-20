package zm.gov.moh.lisservice.lab.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.lab.entity.LabType;

@Repository
public interface LabTypeRepository extends R2dbcRepository<LabType, Short> {
    Flux<LabType> findByIsActiveTrue(Pageable pageable);
    Mono<Long> countByIsActiveTrue();
    Mono<Boolean> existsByNameIgnoreCase(String name);
}
