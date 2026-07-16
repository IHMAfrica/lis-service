package moh.gov.zm.lis.disa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.lab.entity.OrderStatus;
import moh.gov.zm.lis.lab.entity.ShippingOrder;
import moh.gov.zm.lis.lab.repository.OrderStatusRepository;
import moh.gov.zm.lis.lab.repository.ShippingOrderRepository;
import moh.gov.zm.lis.messaging.service.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Ingests a resolved DISA lab order: builds the HL7 {@code OML^O21}, enqueues it
 * to the transactional outbox (event {@code LAB_ORDER_CREATED}, relayed to the
 * lab-orders topic by {@code OutboxRelayScheduler}) and records an
 * {@link OrderStatus} row — both in the same transaction, so an order is either
 * fully accepted (and guaranteed to publish) or not at all.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DisaLabOrderService {
    private static final String LAB_ORDER_CREATED = "LAB_ORDER_CREATED";

    private final DisaLogResolutionService resolutionService;
    private final Oml021Builder oml021Builder;
    private final DomainEventPublisher domainEventPublisher;
    private final OrderStatusRepository orderStatusRepository;
    private final ShippingOrderRepository shippingOrderRepository;

    @Transactional
    public Mono<OrderStatus> ingest(DisaLogPayload payload, UUID correlationId) {
        return resolutionService.resolve(payload).flatMap(resolved -> {
            String messageControlId = UUID.randomUUID().toString();

            String hl7;
            try {
                hl7 = oml021Builder.encode(oml021Builder.build(resolved, messageControlId));
            } catch (Exception e) {
                return Mono.error(new IllegalStateException("Failed to build HL7 OML^O21: " + e.getMessage(), e));
            }

            OrderStatus status = buildOrderStatus(resolved, messageControlId, correlationId);
            ShippingOrder shipping = buildShippingOrder(resolved, correlationId);

            return domainEventPublisher.publish(LAB_ORDER_CREATED, hl7, null, correlationId)
                    .then(orderStatusRepository.save(status))
                    .flatMap(saved -> shippingOrderRepository.save(shipping).thenReturn(saved))
                    .doOnSuccess(saved -> {
                        assert saved != null;
                        log.info("Enqueued lab order '{}' (msgControlId={}, lab={}, correlationId={})",
                                saved.getOrderId(), saved.getMessageControlId(), saved.getLabCode(), correlationId);
                    });
        });
    }

    private ShippingOrder buildShippingOrder(ResolvedDisaLog resolved, UUID correlationId) {
        DisaLogPayload p = resolved.message();
        String fullName = ((emptyToBlank(p.getPatientFirstName()) + " " + emptyToBlank(p.getPatientSurname()))
                .trim());
        return ShippingOrder.builder()
                .orderId(p.getOrderNumber())
                .correlationId(correlationId)
                .mflCode(resolved.mflCode())
                .labCode(resolved.labCode())
                .fullName(fullName.isEmpty() ? null : fullName)
                .age(p.getPatientAge())
                .sex(p.getGender() == 1 ? "M" : "F")
                .testType(p.getInvestigationTestName())
                .requestedDate(p.getRequestedDate())
                .specimenCollectedDate(p.getSpecimenCollectedDate())
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private static String emptyToBlank(String s) {
        return s == null ? "" : s;
    }

    private OrderStatus buildOrderStatus(ResolvedDisaLog resolved, String messageControlId, UUID correlationId) {
        DisaLogPayload p = resolved.message();

        LocalDate orderDate;
        LocalTime orderTime;
        LocalDateTime collectedAt = p.getInvestigationSampleCollectionDate();
        if (collectedAt != null) {
            orderDate = collectedAt.toLocalDate();
            orderTime = collectedAt.toLocalTime();
        } else {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            orderDate = now.toLocalDate();
            orderTime = now.toLocalTime();
        }

        return OrderStatus.builder()
                .messageControlId(messageControlId)
                .orderId(p.getOrderNumber())
                .orderDate(orderDate)
                .orderTime(orderTime)
                .mflCode(resolved.mflCode())
                .labCode(resolved.labCode())
                .patientIdentifier(p.getPatientNUPN())
                .testLoinc(resolved.loincCode())
                .correlationId(correlationId)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
