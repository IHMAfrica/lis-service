package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.Laboratory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface LaboratoryRepository extends R2dbcRepository<Laboratory, Short> {
    Mono<Boolean> existsByLabCode(String labCode);
}
