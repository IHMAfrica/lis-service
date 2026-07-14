package moh.gov.zm.lis.ref.repository;

import moh.gov.zm.lis.ref.entity.OutboundEventType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface OutboundEventTypeRepository extends R2dbcRepository<OutboundEventType, Short> {
    Mono<OutboundEventType> findByCode(String code);
}
