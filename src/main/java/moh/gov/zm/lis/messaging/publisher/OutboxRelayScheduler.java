package moh.gov.zm.lis.messaging.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.messaging.entity.OutboundEventOutbox;
import moh.gov.zm.lis.messaging.repository.OutboundEventOutboxRepository;
import moh.gov.zm.lis.redis.lock.DistributedLockService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {
    private final OutboundEventOutboxRepository outboxRepository;
    private final DistributedLockService lockService;
    private final KafkaSender<String, String> kafkaSender;

    @Scheduled(fixedDelayString = "1000")
    public void relay() {
        int batchSize = 50;
        short maxRetries = 5;

        lockService.withLock(DistributedLockService.OUTBOX_RELAY_LOCK, Duration.ofMillis(30_000),
                outboxRepository
                        .findAllByIsPublishedFalseAndRetryCountLessThanOrderByCreatedAtAsc(maxRetries)
                        .take(batchSize)
                        .concatMap(row -> publishRow(row, maxRetries))
                        .then()
        ).block();
    }

    private Mono<Void> publishRow(OutboundEventOutbox row, short maxRetries) {
        String correlationId = row.getCorrelationId().toString();
        // Key by correlation id so all messages for one order/result keep partition
        // ordering; the same value is carried as correlation metadata for the result callback.
        ProducerRecord<String, String> record = new ProducerRecord<>(row.getTopic(), correlationId, row.getPayload());
        SenderRecord<String, String, String> senderRecord = SenderRecord.create(record, correlationId);

        return kafkaSender.send(Mono.just(senderRecord))
                .next()
                .flatMap(result -> {
                    if (result.exception() != null) {
                        return Mono.error(result.exception());
                    }
                    log.debug("Kafka send succeeded [topic={}, partition={}, offset={}]",
                            result.recordMetadata().topic(),
                            result.recordMetadata().partition(),
                            result.recordMetadata().offset());
                    return markPublished(row);
                })
                .onErrorResume(ex -> recordPublishFailure(row, ex.getMessage(), maxRetries));
    }

    private Mono<Void> markPublished(OutboundEventOutbox row) {
        row.setIsPublished(true);
        row.setPublishedAt(OffsetDateTime.now());
        row.setLastError(null);

        return outboxRepository.save(row)
                .doOnSuccess(saved -> {
                    assert saved != null;
                    log.debug("Published outbox row: id={}, topic={}", saved.getId(), saved.getTopic());
                })
                .then();
    }

    private Mono<Void> recordPublishFailure(OutboundEventOutbox row, String errorMessage, short maxRetries) {
        short newCount = (short) (row.getRetryCount() + 1);
        row.setRetryCount(newCount);
        row.setLastError(errorMessage);

        return outboxRepository.save(row)
                .doOnSuccess(saved -> {
                    assert saved != null;
                    if (newCount >= maxRetries) {
                        log.error("Outbox row exhausted retries — manual intervention required: id={}, topic={}, retries={}, error={}",
                                saved.getId(), saved.getTopic(), newCount, errorMessage);
                    } else {
                        log.warn("Outbox row publish failed (attempt {}/{}): id={}, error={}",
                                newCount, maxRetries, saved.getId(), errorMessage);
                    }
                })
                .then();
    }
}
