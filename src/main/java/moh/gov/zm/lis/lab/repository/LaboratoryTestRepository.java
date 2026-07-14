package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.LaboratoryTest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface LaboratoryTestRepository extends R2dbcRepository<LaboratoryTest, UUID> {
    Mono<Boolean> existsByLaboratoryIdAndTestId(Short laboratoryId, Long testId);

    Flux<LaboratoryTest> findAllByLaboratoryIdIn(Collection<Short> laboratoryIds);
}
