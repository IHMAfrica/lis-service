package moh.gov.zm.lis.messaging.repository;

import moh.gov.zm.lis.messaging.entity.InboundEventLog;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface InboundEventLogRepository extends R2dbcRepository<InboundEventLog, UUID> {
    Mono<InboundEventLog> findByMessageId(String messageId);

    Flux<InboundEventLog> findAllByProcessingStatus(String processingStatus);

    Flux<InboundEventLog> findAllByProcessingStatusAndReceivedAtBefore(
            String processingStatus, OffsetDateTime cutoff);

    Flux<InboundEventLog> findAllBySourceService(String sourceService);

    Flux<InboundEventLog> findAllByEventTypeCode(String eventTypeCode);

    Mono<Boolean> existsByMessageId(String messageId);
}
