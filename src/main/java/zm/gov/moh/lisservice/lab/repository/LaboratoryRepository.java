package zm.gov.moh.lisservice.lab.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.lab.entity.Laboratory;

@Repository
public interface LaboratoryRepository extends R2dbcRepository<Laboratory, Short> {
    Flux<Laboratory> findByIsActiveTrue(Pageable pageable);
    Mono<Long> countByIsActiveTrue();
    Mono<Boolean> existsByLabCodeAndIsActiveTrue(String labCode);
    Mono<Laboratory> findByIdAndIsActiveTrue(Short id);
}
