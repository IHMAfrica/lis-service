package moh.gov.zm.lis.messaging.service.impl;

import moh.gov.zm.lis.messaging.dto.OutboundEventOutboxDTO;
import moh.gov.zm.lis.messaging.entity.OutboundEventOutbox;
import moh.gov.zm.lis.messaging.repository.OutboundEventOutboxRepository;
import moh.gov.zm.lis.ref.entity.OutboundEventType;
import moh.gov.zm.lis.ref.repository.OutboundEventTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboundEventServiceImplTest {

    @Mock
    private OutboundEventOutboxRepository outboxRepository;
    @Mock
    private OutboundEventTypeRepository outboundEventTypeRepository;

    @InjectMocks
    private OutboundEventServiceImpl service;

    @Test
    void enqueueEvent_savesUnpublishedRowAndMapsResponseWithTypeCode() {
        UUID id = UUID.randomUUID();
        UUID correlation = UUID.randomUUID();
        ArgumentCaptor<OutboundEventOutbox> captor = ArgumentCaptor.forClass(OutboundEventOutbox.class);

        when(outboxRepository.save(captor.capture())).thenAnswer(inv -> {
            OutboundEventOutbox row = inv.getArgument(0);
            row.setId(id);
            return Mono.just(row);
        });
        when(outboundEventTypeRepository.findById((short) 1))
                .thenReturn(Mono.just(OutboundEventType.builder().id((short) 1).code("LAB_ORDER_CREATED").name("Lab order created").build()));

        var request = OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest.builder()
                .eventTypeId((short) 1)
                .topic("lab-orders")
                .payload("hl7")
                .correlationId(correlation)
                .build();

        StepVerifier.create(service.enqueueEvent(request))
                .assertNext(resp -> {
                    assertThat(resp.getId()).isEqualTo(id);
                    assertThat(resp.getEventTypeCode()).isEqualTo("LAB_ORDER_CREATED");
                    assertThat(resp.getTopic()).isEqualTo("lab-orders");
                    assertThat(resp.getCorrelationId()).isEqualTo(correlation);
                    assertThat(resp.getIsPublished()).isFalse();
                })
                .verifyComplete();

        OutboundEventOutbox saved = captor.getValue();
        assertThat(saved.getIsPublished()).isFalse();
        assertThat(saved.getRetryCount()).isEqualTo((short) 0);
        assertThat(saved.getPayload()).isEqualTo("hl7");
    }

    @Test
    void enqueueEvent_mapsResponseEvenWhenEventTypeMissing() {
        when(outboxRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(outboundEventTypeRepository.findById(any(Short.class))).thenReturn(Mono.empty());

        var request = OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest.builder()
                .eventTypeId((short) 9).topic("lis.dlq").payload("x").correlationId(UUID.randomUUID()).build();

        StepVerifier.create(service.enqueueEvent(request))
                .assertNext(resp -> {
                    assertThat(resp.getEventTypeCode()).isNull();
                    assertThat(resp.getTopic()).isEqualTo("lis.dlq");
                })
                .verifyComplete();
    }

    @Test
    void countPending_delegatesToRepository() {
        when(outboxRepository.countByIsPublishedFalse()).thenReturn(Mono.just(7L));

        StepVerifier.create(service.countPending()).expectNext(7L).verifyComplete();
    }
}
