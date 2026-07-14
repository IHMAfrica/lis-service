package moh.gov.zm.lis.messaging.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import moh.gov.zm.lis.messaging.dto.OutboundEventOutboxDTO;
import moh.gov.zm.lis.messaging.publisher.LisTopicRouter;
import moh.gov.zm.lis.messaging.service.OutboundEventService;
import moh.gov.zm.lis.redis.cache.ReferenceDataCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherImplTest {

    @Mock
    private OutboundEventService outboundEventService;
    @Mock
    private ReferenceDataCache referenceDataCache;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LisTopicRouter topicRouter = new LisTopicRouter("lis.dlq", "lab-orders", "lab-result-acks");

    private DomainEventPublisherImpl publisher() {
        return new DomainEventPublisherImpl(outboundEventService, referenceDataCache, objectMapper, topicRouter);
    }

    @Test
    void publish_enqueuesStringPayloadVerbatimWithResolvedTopic() {
        UUID correlation = UUID.randomUUID();
        ArgumentCaptor<OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest> captor =
                ArgumentCaptor.forClass(OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest.class);
        when(referenceDataCache.getOutboundEventTypeId("LAB_ORDER_CREATED")).thenReturn(Mono.just((short) 1));
        when(outboundEventService.enqueueEvent(captor.capture()))
                .thenReturn(Mono.just(OutboundEventOutboxDTO.OutboundEventOutboxResponse.builder().build()));

        StepVerifier.create(publisher().publish("LAB_ORDER_CREATED", "raw-hl7", null, correlation))
                .verifyComplete();

        var req = captor.getValue();
        assertThat(req.getEventTypeId()).isEqualTo((short) 1);
        assertThat(req.getTopic()).isEqualTo("lab-orders");
        assertThat(req.getPayload()).isEqualTo("raw-hl7");
        assertThat(req.getCorrelationId()).isEqualTo(correlation);
        assertThat(req.getCreatedBy()).isNull();
    }

    @Test
    void publish_serializesNonStringPayloadToJson() {
        ArgumentCaptor<OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest> captor =
                ArgumentCaptor.forClass(OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest.class);
        when(referenceDataCache.getOutboundEventTypeId("LAB_RESULT_ACK_CREATED")).thenReturn(Mono.just((short) 2));
        when(outboundEventService.enqueueEvent(captor.capture()))
                .thenReturn(Mono.just(OutboundEventOutboxDTO.OutboundEventOutboxResponse.builder().build()));

        StepVerifier.create(publisher().publish("LAB_RESULT_ACK_CREATED", Map.of("k", "v"), null, UUID.randomUUID()))
                .verifyComplete();

        assertThat(captor.getValue().getPayload()).isEqualTo("{\"k\":\"v\"}");
        assertThat(captor.getValue().getTopic()).isEqualTo("lab-result-acks");
    }

    @Test
    void publish_errorsOnUnknownEventType() {
        when(referenceDataCache.getOutboundEventTypeId("NOPE")).thenReturn(Mono.empty());

        StepVerifier.create(publisher().publish("NOPE", "x", null, UUID.randomUUID()))
                .expectError(IllegalStateException.class)
                .verify();

        verify(outboundEventService, never()).enqueueEvent(any());
    }
}
