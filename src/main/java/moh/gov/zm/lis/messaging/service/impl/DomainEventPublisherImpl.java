package moh.gov.zm.lis.messaging.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.messaging.dto.OutboundEventOutboxDTO;
import moh.gov.zm.lis.messaging.publisher.LisTopicRouter;
import moh.gov.zm.lis.messaging.service.DomainEventPublisher;
import moh.gov.zm.lis.messaging.service.OutboundEventService;
import moh.gov.zm.lis.redis.cache.ReferenceDataCache;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainEventPublisherImpl implements DomainEventPublisher {
    private final OutboundEventService outboundEventService;
    private final ReferenceDataCache referenceDataCache;
    private final ObjectMapper objectMapper;
    private final LisTopicRouter topicRouter;

    @Override
    public Mono<Void> publish(String eventTypeCode, Object payload, UUID causedBy, UUID correlationId) {
        UUID correlation = correlationId != null ? correlationId : UUID.randomUUID();

        return referenceDataCache.getOutboundEventTypeId(eventTypeCode)
                .switchIfEmpty(Mono.error(new IllegalStateException("Unknown outbound event type code: " + eventTypeCode)))
                .flatMap(eventTypeId -> serialize(payload)
                        .flatMap(value -> outboundEventService.enqueueEvent(
                                OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest.builder()
                                        .eventTypeId(eventTypeId)
                                        .topic(topicRouter.resolve(eventTypeCode))
                                        .payload(value)
                                        .correlationId(correlation)
                                        .createdBy(causedBy)
                                        .build())))
                .doOnSuccess(v -> log.debug("Enqueued outbound event '{}' (correlationId={})", eventTypeCode, correlation))
                .then();
    }

    /**
     * Reduce a payload to the raw string that will be relayed to Kafka verbatim.
     * HL7 v2.5 payloads are already text, so strings are passed through unchanged;
     * any other object is serialized to JSON as a fallback.
     */
    private Mono<String> serialize(Object payload) {
        if (payload instanceof String s) {
            return Mono.just(s);
        }
        try {
            return Mono.just(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            return Mono.error(new IllegalStateException("Failed to serialize outbound event payload", e));
        }
    }
}
