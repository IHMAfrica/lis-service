package moh.gov.zm.lis.streaming.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.messaging.dto.InboundEventLogDTO;
import moh.gov.zm.lis.messaging.service.InboundEventService;
import moh.gov.zm.lis.redis.idempotency.MessageIdempotencyCache;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * Base for Kafka Streams event handlers. Enforces exactly-once-effect processing
 * with a two-tier idempotency check (Redis cache, then the {@code inbound_event_log}
 * table), records every consumed message in {@code inbound_event_log}, then delegates
 * to {@link #dispatch} for the domain work and marks the message processed.
 *
 * <p>Unlike the JSON-envelope original, the message identity (id / type / source /
 * correlation) is extracted by the subclass via {@link #describe}, so any wire
 * format works — here, HL7 v2.5 text.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class StreamEventProcessor {
    private final MessageIdempotencyCache idempotencyCache;
    private final InboundEventService inboundEventService;

    /** Kafka Streams entry point — synchronous, so the reactive pipeline is blocked on the stream thread. */
    public void handle(String key, String value) {
        if (value == null || value.isBlank()) {
            log.warn("Received blank/null Kafka message — key='{}', skipping", key);
            return;
        }
        buildPipeline(value)
                .subscribeOn(Schedulers.boundedElastic())
                .block();
    }

    private Mono<Void> buildPipeline(String rawValue) {
        return Mono.fromCallable(() -> describe(rawValue))
                .flatMap(descriptor -> idempotencyCache.isProcessed(descriptor.messageId())
                        .flatMap(cached -> {
                            if (cached) {
                                log.debug("Skipping duplicate (Redis hit) — messageId='{}', type='{}'",
                                        descriptor.messageId(), descriptor.eventType());
                                return Mono.<Void>empty();
                            }
                            return checkDbAndProcess(descriptor, rawValue);
                        }))
                .onErrorResume(ex -> {
                    log.error("Unhandled error in stream processor for message '{}': {}",
                            rawValue.substring(0, Math.min(rawValue.length(), 200)), ex.getMessage(), ex);
                    return Mono.empty();
                });
    }

    private Mono<Void> checkDbAndProcess(EventDescriptor descriptor, String rawValue) {
        return inboundEventService.isAlreadyProcessed(descriptor.messageId())
                .flatMap(dbHit -> {
                    if (dbHit) {
                        log.debug("Skipping duplicate (DB hit) — messageId='{}', type='{}'",
                                descriptor.messageId(), descriptor.eventType());
                        return idempotencyCache.markProcessed(descriptor.messageId());
                    }
                    return processEvent(descriptor, rawValue);
                });
    }

    private Mono<Void> processEvent(EventDescriptor descriptor, String rawValue) {
        return inboundEventService.recordEvent(InboundEventLogDTO.RecordInboundEventRequest.builder()
                        .messageId(descriptor.messageId())
                        .topic(topicName())
                        .eventTypeCode(descriptor.eventType())
                        .sourceService(descriptor.sourceService())
                        .correlationId(descriptor.correlationId())
                        .payload(rawValue)
                        .build())
                .flatMap(_ -> dispatch(descriptor, rawValue)
                        .then(inboundEventService.markProcessed(descriptor.messageId()))
                        .then(idempotencyCache.markProcessed(descriptor.messageId()))
                        .doOnSuccess(_ -> log.info("Processed event — messageId='{}', type='{}'",
                                descriptor.messageId(), descriptor.eventType()))
                        .onErrorResume(ex -> {
                            log.error("Failed to process event — messageId='{}', type='{}': {}",
                                    descriptor.messageId(), descriptor.eventType(), ex.getMessage(), ex);
                            return inboundEventService.markFailed(descriptor.messageId(), ex.getMessage())
                                    .then(idempotencyCache.evict(descriptor.messageId()))
                                    .then(onProcessingFailure(descriptor, rawValue, ex));
                        }))
                .then();
    }

    /** Topic this handler consumes; registered by {@code LisStreamTopology}. */
    public abstract String topicName();

    /** Extract the message identity from the raw payload (HL7, JSON, …). */
    protected abstract EventDescriptor describe(String rawValue) throws Exception;

    /** Perform the domain work for a not-yet-processed message. */
    protected abstract Mono<Void> dispatch(EventDescriptor descriptor, String rawValue);

    /**
     * Hook invoked when {@link #dispatch} fails (after the event is marked FAILED).
     * Subclasses may emit a negative acknowledgement, etc. Default: no-op. Must be
     * best-effort — it runs on the failure path and its own errors are ignored.
     */
    protected Mono<Void> onProcessingFailure(EventDescriptor descriptor, String rawValue, Throwable error) {
        return Mono.empty();
    }

    /** Message identity + routing metadata extracted from a consumed message. */
    public record EventDescriptor(String messageId, String eventType, String sourceService, UUID correlationId) {
    }
}
