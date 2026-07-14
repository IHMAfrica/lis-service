package moh.gov.zm.lis.messaging.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Publishes a domain event by enqueueing its payload to the transactional
 * outbox. The payload is stored verbatim (HL7 v2.5 text is passed through as-is;
 * any other object is serialized to JSON). Called inside a service's transaction
 * so the event is persisted atomically with the state change it accompanies; the
 * {@link moh.gov.zm.lis.messaging.publisher.OutboxRelayScheduler} later relays it
 * to Kafka.
 */
public interface DomainEventPublisher {
    Mono<Void> publish(String eventTypeCode, Object payload, UUID causedBy, UUID correlationId);
}
