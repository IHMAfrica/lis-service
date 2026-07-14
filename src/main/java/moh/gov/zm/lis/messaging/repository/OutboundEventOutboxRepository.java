package moh.gov.zm.lis.messaging.repository;

import moh.gov.zm.lis.messaging.entity.OutboundEventOutbox;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OutboundEventOutboxRepository extends R2dbcRepository<OutboundEventOutbox, UUID> {
    Flux<OutboundEventOutbox> findAllByIsPublishedFalseOrderByCreatedAtAsc();

    Flux<OutboundEventOutbox> findAllByIsPublishedFalseAndRetryCountLessThanOrderByCreatedAtAsc(short maxRetries);

    Mono<Long> countByIsPublishedFalse();
}
