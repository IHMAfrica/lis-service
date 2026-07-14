package moh.gov.zm.lis.ref.repository;

import moh.gov.zm.lis.ref.entity.InboundEventType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface InboundEventTypeRepository extends R2dbcRepository<InboundEventType, Short> {
    Mono<InboundEventType> findByCode(String code);
}
