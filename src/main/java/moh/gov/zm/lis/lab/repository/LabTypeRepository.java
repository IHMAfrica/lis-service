package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.LabType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface LabTypeRepository extends R2dbcRepository<LabType, Short> {
    Mono<Boolean> existsByName(String name);
}
