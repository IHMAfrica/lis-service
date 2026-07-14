package moh.gov.zm.lis.streaming.handler;

import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.config.ConfigProperties;
import moh.gov.zm.lis.disa.service.LabOrderAck;
import moh.gov.zm.lis.disa.service.LabOrderAckParser;
import moh.gov.zm.lis.disa.service.LabOrderAckService;
import moh.gov.zm.lis.messaging.service.InboundEventService;
import moh.gov.zm.lis.redis.idempotency.MessageIdempotencyCache;
import moh.gov.zm.lis.streaming.processor.StreamEventProcessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Consumes HL7 lab-order acknowledgements on the lab-orders-ack topic. The ACK's
 * MSH-10 is the message id (idempotency key); MSA-2 (the original order's MSH-10)
 * is carried as the correlation id. Recording + facility notification are done by
 * {@link LabOrderAckService}.
 */
@Slf4j
@Component
public class LabOrderAckHandler extends StreamEventProcessor {
    private static final String EVENT_TYPE = "LAB_ORDER_ACK_CREATED";
    private static final String SOURCE = "disa";

    private final ConfigProperties configProperties;
    private final LabOrderAckParser parser;
    private final LabOrderAckService labOrderAckService;

    public LabOrderAckHandler(MessageIdempotencyCache idempotencyCache,
                              InboundEventService inboundEventService,
                              ConfigProperties configProperties,
                              LabOrderAckParser parser,
                              LabOrderAckService labOrderAckService) {
        super(idempotencyCache, inboundEventService);
        this.configProperties = configProperties;
        this.parser = parser;
        this.labOrderAckService = labOrderAckService;
    }

    @Override
    public String topicName() {
        return configProperties.getLabOrdersAckTopic();
    }

    @Override
    protected EventDescriptor describe(String rawValue) throws Exception {
        LabOrderAck ack = parser.parse(rawValue);
        if (ack.messageControlId() == null) {
            throw new IllegalArgumentException("Lab-order ACK is missing its MSH-10 message control id");
        }
        return new EventDescriptor(ack.messageControlId(), EVENT_TYPE, SOURCE, tryUuid(ack.refMessageControlId()));
    }

    @Override
    protected Mono<Void> dispatch(EventDescriptor descriptor, String rawValue) {
        return Mono.fromCallable(() -> parser.parse(rawValue))
                .flatMap(labOrderAckService::process);
    }

    private static UUID tryUuid(String value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
