package moh.gov.zm.lis.disa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import moh.gov.zm.lis.disa.forward.LabResultForwarder;
import moh.gov.zm.lis.lab.entity.LabResult;
import moh.gov.zm.lis.lab.entity.OrderStatus;
import moh.gov.zm.lis.lab.repository.LabResultObservationRepository;
import moh.gov.zm.lis.lab.repository.LabResultRepository;
import moh.gov.zm.lis.lab.repository.OrderStatusRepository;
import moh.gov.zm.lis.messaging.service.DomainEventPublisher;
import moh.gov.zm.lis.notify.service.NotificationDispatcher;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.reactivestreams.Publisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LabResultServiceTest {

    private static final String MFL = "504010";
    private static final String LOINC = "20447-9";

    @Mock private OrderStatusRepository orderStatusRepository;
    @Mock private LabResultRepository labResultRepository;
    @Mock private LabResultObservationRepository observationRepository;
    @Mock private R2dbcEntityTemplate template;
    @Mock private TransactionalOperator txOperator;
    @Mock private FacilityService facilityService;
    @Mock private NotificationDispatcher notificationDispatcher;
    @Mock private LabResultForwarder forwarder;
    @Mock private DomainEventPublisher domainEventPublisher;

    // Real collaborators — pure, no external deps.
    private final LabResultAckBuilder ackBuilder = new LabResultAckBuilder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private LabResultService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        service = new LabResultService(orderStatusRepository, labResultRepository, observationRepository,
                template, txOperator, facilityService, notificationDispatcher, forwarder, ackBuilder,
                domainEventPublisher, objectMapper);

        // transactional operator is a pass-through in the unit test
        when(txOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
        // save assigns a generated id
        when(labResultRepository.save(any(LabResult.class))).thenAnswer(inv -> {
            LabResult r = inv.getArgument(0);
            if (r.getId() == null) {
                r.setId(UUID.randomUUID());
            }
            return Mono.just(r);
        });
        when(labResultRepository.findAllByResultKey(any())).thenReturn(Flux.empty());
        when(observationRepository.saveAll(any(Publisher.class))).thenReturn(Flux.empty());
        when(template.update(any(Query.class), any(Update.class), eq(LabResult.class))).thenReturn(Mono.just(0L));
        when(template.select(any(Query.class), eq(OrderStatus.class))).thenReturn(Flux.empty());
        when(forwarder.forward(any())).thenReturn(Mono.empty());
        when(domainEventPublisher.publish(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(notificationDispatcher.dispatchToFacility(anyLong(), any())).thenReturn(Mono.empty());
        when(facilityService.cachedAll()).thenReturn(Mono.just(List.of(facility())));
    }

    private Facility facility() {
        return Facility.builder().id(7L).name("Kanyama").mflCode(MFL).isActive(true).build();
    }

    private OrderStatus order() {
        return OrderStatus.builder()
                .id(UUID.randomUUID()).orderId("ORD-1").correlationId(UUID.randomUUID()).mflCode(MFL).build();
    }

    private LabResultMessage message(String placer) {
        return LabResultMessage.builder()
                .messageControlId("ORU-1")
                .sendingFacilityName("Kanyama Lab")
                .orderingMflCode(MFL)
                .patientIdentifier("PAT-1")
                .placerOrderNumber(placer)
                .testLoinc(LOINC)
                .resultStatus("F")
                .observations(List.of(LabResultMessage.Observation.builder()
                        .setId(1).valueType("NM").loinc(LOINC).value("1200")
                        .numericValue(new BigDecimal("1200")).build()))
                .build();
    }

    private LabResult saved() {
        ArgumentCaptor<LabResult> captor = ArgumentCaptor.forClass(LabResult.class);
        verify(labResultRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void primaryMatchReconcilesForwardsAndNotifies() {
        when(orderStatusRepository.findFirstByOrderIdOrderByCreatedAtDesc("ORD-1")).thenReturn(Mono.just(order()));

        StepVerifier.create(service.process(message("ORD-1"), "raw")).verifyComplete();

        LabResult r = saved();
        assertThat(r.getReconciliationStatus()).isEqualTo("RECONCILED");
        assertThat(r.getMatchMethod()).isEqualTo("PLACER");
        assertThat(r.getForwardStatus()).isEqualTo("PENDING");
        assertThat(r.getReviewStatus()).isEqualTo("NONE");
        verify(forwarder).forward(any());
        verify(notificationDispatcher).dispatchToFacility(eq(7L), any());
        // positive ACK enqueued to the outbox
        verify(domainEventPublisher).publish(eq("LAB_RESULT_ACK_CREATED"), any(), any(), any());
    }

    @Test
    void unsolicitedWithObservationsHeldForReviewNotForwarded() {
        // no placer, and secondary lookup returns no candidates
        StepVerifier.create(service.process(message(null), "raw")).verifyComplete();

        LabResult r = saved();
        assertThat(r.getReconciliationStatus()).isEqualTo("UNSOLICITED");
        assertThat(r.getMatchMethod()).isEqualTo("NONE");
        assertThat(r.getForwardStatus()).isEqualTo("NOT_APPLICABLE");
        assertThat(r.getReviewStatus()).isEqualTo("PENDING_REVIEW");
        verify(forwarder, never()).forward(any());
        // review-required notification still goes to the facility
        verify(notificationDispatcher).dispatchToFacility(eq(7L), any());
    }

    @Test
    void secondaryMatchOnSingleCandidateReconciles() {
        when(template.select(any(Query.class), eq(OrderStatus.class))).thenReturn(Flux.just(order()));

        StepVerifier.create(service.process(message(null), "raw")).verifyComplete();

        LabResult r = saved();
        assertThat(r.getReconciliationStatus()).isEqualTo("RECONCILED");
        assertThat(r.getMatchMethod()).isEqualTo("SECONDARY");
        verify(forwarder).forward(any());
    }

    @Test
    void multipleSecondaryCandidatesStayUnsolicited() {
        when(template.select(any(Query.class), eq(OrderStatus.class)))
                .thenReturn(Flux.just(order(), order()));

        StepVerifier.create(service.process(message(null), "raw")).verifyComplete();

        LabResult r = saved();
        assertThat(r.getReconciliationStatus()).isEqualTo("UNSOLICITED");
        assertThat(r.getCandidateCount()).isEqualTo(2);
        verify(forwarder, never()).forward(any());
    }

    @Test
    void duplicateMessageIsSwallowed() {
        when(orderStatusRepository.findFirstByOrderIdOrderByCreatedAtDesc("ORD-1")).thenReturn(Mono.just(order()));
        when(labResultRepository.save(any())).thenReturn(Mono.error(
                new DataIntegrityViolationException("duplicate key value violates unique constraint \"uq_lab_result_message_control\"")));

        // idempotent re-delivery: completes empty, no error, no forward
        StepVerifier.create(service.process(message("ORD-1"), "raw")).verifyComplete();
        verify(forwarder, never()).forward(any());
    }

    @Test
    void genuineIntegrityViolationPropagates() {
        when(orderStatusRepository.findFirstByOrderIdOrderByCreatedAtDesc("ORD-1")).thenReturn(Mono.just(order()));
        when(labResultRepository.save(any())).thenReturn(Mono.error(
                new DataIntegrityViolationException("null value in column \"lab_code\" violates not-null constraint")));

        // a real data failure must surface (so the handler emits a negative ACK)
        StepVerifier.create(service.process(message("ORD-1"), "raw"))
                .expectError(DataIntegrityViolationException.class)
                .verify();
    }
}
