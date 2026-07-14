package moh.gov.zm.lis.streaming.handler;

import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.config.ConfigProperties;
import moh.gov.zm.lis.disa.service.LabResultAckBuilder;
import moh.gov.zm.lis.disa.service.LabResultMessage;
import moh.gov.zm.lis.disa.service.LabResultParser;
import moh.gov.zm.lis.disa.service.LabResultService;
import moh.gov.zm.lis.messaging.service.DomainEventPublisher;
import moh.gov.zm.lis.messaging.service.InboundEventService;
import moh.gov.zm.lis.redis.idempotency.MessageIdempotencyCache;
import moh.gov.zm.lis.streaming.processor.StreamEventProcessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Consumes HL7 ORU^R01 lab results on the lab-results topic. Idempotency key is the
 * ORU MSH-10; the reconcile / persist / forward / notify work — including the
 * positive (AA) acknowledgement — is delegated to {@link LabResultService}. When
 * processing fails, an error (AE) acknowledgement is emitted here.
 */
@Slf4j
@Component
public class LabResultHandler extends StreamEventProcessor {
    private static final String EVENT_TYPE = "LAB_RESULT_CREATED";
    private static final String SOURCE = "disa";
    private static final String ACK_EVENT_TYPE = "LAB_RESULT_ACK_CREATED";
    // Generic text for the outbound ACK — the detailed cause is logged and stored in inbound_event_log,
    // not leaked to the external system.
    private static final String ERROR_TEXT = "Error processing lab result";

    private final ConfigProperties configProperties;
    private final LabResultParser parser;
    private final LabResultService labResultService;
    private final LabResultAckBuilder ackBuilder;
    private final DomainEventPublisher domainEventPublisher;

    public LabResultHandler(MessageIdempotencyCache idempotencyCache,
                            InboundEventService inboundEventService,
                            ConfigProperties configProperties,
                            LabResultParser parser,
                            LabResultService labResultService,
                            LabResultAckBuilder ackBuilder,
                            DomainEventPublisher domainEventPublisher) {
        super(idempotencyCache, inboundEventService);
        this.configProperties = configProperties;
        this.parser = parser;
        this.labResultService = labResultService;
        this.ackBuilder = ackBuilder;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public String topicName() {
        return configProperties.getLabResultsTopic();
    }

    @Override
    protected EventDescriptor describe(String rawValue) {
        LabResultMessage message = parser.parse(rawValue);
        if (message.getMessageControlId() == null) {
            throw new IllegalArgumentException("Lab result is missing its MSH-10 message control id");
        }
        return new EventDescriptor(message.getMessageControlId(), EVENT_TYPE, SOURCE, null);
    }

    @Override
    protected Mono<Void> dispatch(EventDescriptor descriptor, String rawValue) {
        return Mono.fromCallable(() -> parser.parse(rawValue))
                .flatMap(message -> labResultService.process(message, rawValue));
    }

    /** Processing failed: emit an error acknowledgement (ACK^R01, MSA-1 = AE) referencing the message. */
    @Override
    protected Mono<Void> onProcessingFailure(EventDescriptor descriptor, String rawValue, Throwable error) {
        return Mono.fromCallable(() -> parser.parse(rawValue))
                .flatMap(message -> {
                    String ackHl7 = ackBuilder.build(message, "AE", ERROR_TEXT);
                    return domainEventPublisher.publish(ACK_EVENT_TYPE, ackHl7, null, null);
                })
                .doOnSuccess(v -> log.info("Emitted error ACK (AE) for lab result '{}'", descriptor.messageId()))
                .onErrorResume(ex -> {
                    log.warn("Could not emit error ACK for lab result '{}': {}", descriptor.messageId(), ex.getMessage());
                    return Mono.empty();
                });
    }
}
