package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.Test;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface TestRepository extends R2dbcRepository<Test, Long> {
    Mono<Boolean> existsByLoincCode(String loincCode);
}
