package moh.gov.zm.lis.disa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.lab.entity.OrderAckStatus;
import moh.gov.zm.lis.lab.entity.OrderStatus;
import moh.gov.zm.lis.lab.repository.OrderAckStatusRepository;
import moh.gov.zm.lis.lab.repository.OrderStatusRepository;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import moh.gov.zm.lis.notify.service.NotificationDispatcher;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Domain work for a lab-order acknowledgement: record it in
 * {@code lab.order_ack_status} and notify the ordering facility's users.
 *
 * <p>Idempotency (dedup on redelivery) is handled by {@code StreamEventProcessor}
 * via the inbound-event log, so this runs once per ACK. The ordering facility is
 * resolved from the ACK's receiving MFL code (MSH-6.1); MSA-2 links back to the
 * originating order for enrichment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LabOrderAckService {
    private static final String NOTIFICATION_TYPE = "LAB_ORDER_ACK";

    private final OrderAckStatusRepository orderAckStatusRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final FacilityService facilityService;
    private final NotificationDispatcher notificationDispatcher;
    private final ObjectMapper objectMapper;

    public Mono<Void> process(LabOrderAck ack) {
        return record(ack).then(notify(ack));
    }

    private Mono<Void> record(LabOrderAck ack) {
        OrderAckStatus entity = OrderAckStatus.builder()
                .messageControlId(ack.messageControlId())
                .orderAckDate(ack.ackDate())
                .orderAckTime(ack.ackTime())
                .sendingFacilityLabCode(ack.sendingFacilityLabCode())
                .receivingFacilityMflCode(ack.receivingFacilityMflCode())
                .ackCode(ack.ackCode())
                .refMessageControlId(ack.refMessageControlId())
                .textMessage(ack.textMessage())
                .createdAt(OffsetDateTime.now())
                .build();

        return orderAckStatusRepository.save(entity)
                .doOnSuccess(saved -> log.info("Recorded lab-order ACK '{}' (code={}, ref={})",
                        ack.messageControlId(), ack.ackCode(), ack.refMessageControlId()))
                .then();
    }

    private Mono<Void> notify(LabOrderAck ack) {
        return Mono.zip(findOriginatingOrder(ack), resolveFacility(ack.receivingFacilityMflCode()))
                .flatMap(t -> {
                    Optional<OrderStatus> order = t.getT1();
                    Optional<Facility> facility = t.getT2();
                    if (facility.isEmpty()) {
                        log.warn("No facility for MFL code '{}' — ACK '{}' recorded but not notified",
                                ack.receivingFacilityMflCode(), ack.messageControlId());
                        return Mono.empty();
                    }
                    NotificationDTO.DispatchRequest request = buildNotification(ack, order.orElse(null));
                    return notificationDispatcher.dispatchToFacility(facility.get().getId(), request).then();
                });
    }

    private Mono<Optional<OrderStatus>> findOriginatingOrder(LabOrderAck ack) {
        if (ack.refMessageControlId() == null) {
            return Mono.just(Optional.empty());
        }
        return orderStatusRepository.findByMessageControlId(ack.refMessageControlId())
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
    }

    private Mono<Optional<Facility>> resolveFacility(String mflCode) {
        if (mflCode == null) {
            return Mono.just(Optional.empty());
        }
        return facilityService.cachedAll()
                .map(facilities -> facilities.stream()
                        .filter(f -> Boolean.TRUE.equals(f.getIsActive()))
                        .filter(f -> mflCode.equals(f.getMflCode()))
                        .findFirst());
    }

    private NotificationDTO.DispatchRequest buildNotification(LabOrderAck ack, OrderStatus order) {
        String orderId = order != null ? order.getOrderId() : null;
        String title = switch (ack.ackCode() == null ? "" : ack.ackCode()) {
            case "AA" -> "Lab order accepted";
            case "AE" -> "Lab order error";
            case "AR" -> "Lab order rejected";
            default -> "Lab order acknowledgement";
        };
        String body = ack.textMessage() != null
                ? ack.textMessage()
                : title + (orderId != null ? " for order " + orderId : "");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ackCode", ack.ackCode());
        data.put("refMessageControlId", ack.refMessageControlId());
        data.put("ackMessageControlId", ack.messageControlId());
        data.put("sendingLabCode", ack.sendingFacilityLabCode());
        if (orderId != null) {
            data.put("orderId", orderId);
        }
        if (ack.textMessage() != null) {
            data.put("textMessage", ack.textMessage());
        }

        return NotificationDTO.DispatchRequest.builder()
                .type(NOTIFICATION_TYPE)
                .title(title)
                .body(body)
                .data(toJson(data))
                .correlationId(order != null ? order.getCorrelationId() : null)
                .build();
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to serialize notification data: {}", e.getMessage());
            return null;
        }
    }
}
