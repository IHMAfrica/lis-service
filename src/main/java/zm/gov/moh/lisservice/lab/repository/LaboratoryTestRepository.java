package zm.gov.moh.lisservice.lab.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.lab.entity.LaboratoryTest;

import java.util.UUID;

@Repository
public interface LaboratoryTestRepository extends R2dbcRepository<LaboratoryTest, UUID> {
    Flux<LaboratoryTest> findByIsActiveTrue(Pageable pageable);
    Mono<Long> countByIsActiveTrue();
    Flux<LaboratoryTest> findByLaboratoryIdAndIsActiveTrue(Short laboratoryId, Pageable pageable);
    Mono<Long> countByLaboratoryIdAndIsActiveTrue(Short laboratoryId);
    Mono<LaboratoryTest> findByIdAndIsActiveTrue(UUID id);
}
