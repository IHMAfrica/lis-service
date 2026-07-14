package moh.gov.zm.lis.disa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.disa.forward.LabResultForwarder;
import moh.gov.zm.lis.lab.entity.LabResult;
import moh.gov.zm.lis.lab.entity.LabResultObservation;
import moh.gov.zm.lis.lab.entity.OrderStatus;
import moh.gov.zm.lis.lab.repository.LabResultObservationRepository;
import moh.gov.zm.lis.lab.repository.LabResultRepository;
import moh.gov.zm.lis.lab.repository.OrderStatusRepository;
import moh.gov.zm.lis.messaging.service.DomainEventPublisher;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import moh.gov.zm.lis.notify.service.NotificationDispatcher;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persists a received lab result, reconciles it to a lab order, versions it against
 * prior results for the same logical test, and — for the current, confirmed result
 * carrying valid observations — forwards it downstream and notifies the facility.
 *
 * <p>Reconciliation: (1) primary on placer order number (ORC-2/OBR-2) →
 * {@code order_status.order_id}; (2) secondary on patient + LOINC + facility within a
 * 14-day collection-date window (auto-link only on a single candidate).
 *
 * <p>Versioning: successive ORUs for the same {@code result_key} (order|LOINC, or
 * accession|LOINC) chain as versions; the newest is {@code is_current} and older
 * ones are superseded. Only the current version is forwarded / notified, so a late
 * preliminary never overwrites a correction downstream.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabResultService {
    private static final int SECONDARY_WINDOW_DAYS = 14;
    private static final String NOTIFICATION_TYPE = "LAB_RESULT";

    private final OrderStatusRepository orderStatusRepository;
    private final LabResultRepository labResultRepository;
    private final LabResultObservationRepository observationRepository;
    private final R2dbcEntityTemplate template;
    private final TransactionalOperator txOperator;
    private final FacilityService facilityService;
    private final NotificationDispatcher notificationDispatcher;
    private final LabResultForwarder forwarder;
    private final LabResultAckBuilder ackBuilder;
    private final DomainEventPublisher domainEventPublisher;
    private final ObjectMapper objectMapper;

    private static final String ACK_EVENT_TYPE = "LAB_RESULT_ACK_CREATED";

    public Mono<Void> process(LabResultMessage message, String rawMessage) {
        return reconcile(message)
                .flatMap(rec -> persist(message, rawMessage, rec, resultKey(rec, message))
                        .as(txOperator::transactional)
                        .flatMap(saved -> afterPersist(saved, message, rec)))
                .onErrorResume(DataIntegrityViolationException.class, ex -> {
                    // Only the message-control-id uniqueness means "already recorded" (idempotent skip);
                    // any other integrity violation is a genuine failure that must be acknowledged negatively.
                    if (!isDuplicate(ex)) {
                        return Mono.error(ex);
                    }
                    log.debug("Lab result '{}' already recorded — skipping", message.getMessageControlId());
                    return Mono.empty();
                });
    }

    // ---- reconciliation --------------------------------------------------------

    private Mono<Reconciliation> reconcile(LabResultMessage m) {
        Mono<Reconciliation> primary = isBlank(m.getPlacerOrderNumber())
                ? Mono.empty()
                : orderStatusRepository.findFirstByOrderIdOrderByCreatedAtDesc(m.getPlacerOrderNumber())
                .map(order -> Reconciliation.matched("PLACER", order));

        return primary.switchIfEmpty(Mono.defer(() -> secondary(m)));
    }

    private Mono<Reconciliation> secondary(LabResultMessage m) {
        if (isBlank(m.getPatientIdentifier()) || isBlank(m.getTestLoinc()) || isBlank(m.getOrderingMflCode())) {
            return Mono.just(Reconciliation.unsolicited(0));
        }
        Criteria criteria = Criteria.where("patientIdentifier").is(m.getPatientIdentifier())
                .and("testLoinc").is(m.getTestLoinc())
                .and("mflCode").is(m.getOrderingMflCode());
        if (m.getSpecimenCollectedAt() != null) {
            LocalDate collected = m.getSpecimenCollectedAt().toLocalDate();
            criteria = criteria
                    .and("orderDate").greaterThanOrEquals(collected.minusDays(SECONDARY_WINDOW_DAYS))
                    .and("orderDate").lessThanOrEquals(collected.plusDays(SECONDARY_WINDOW_DAYS));
        }
        return template.select(Query.query(criteria).limit(5), OrderStatus.class)
                .collectList()
                .map(candidates -> candidates.size() == 1
                        ? Reconciliation.matched("SECONDARY", candidates.getFirst())
                        : Reconciliation.unsolicited(candidates.size()));
    }

    /** Identity shared by all versions of one logical result. Null = cannot be versioned. */
    private String resultKey(Reconciliation rec, LabResultMessage m) {
        String base;
        if (rec.order() != null) {
            base = "order:" + rec.order().getId();
        } else if (!isBlank(m.getFillerOrderNumber())) {
            base = "filler:" + m.getFillerOrderNumber();
        } else {
            return null;
        }
        return base + "|loinc:" + (m.getTestLoinc() != null ? m.getTestLoinc() : "-");
    }

    // ---- persistence + versioning (transactional) ------------------------------

    private Mono<LabResult> persist(LabResultMessage m, String rawMessage, Reconciliation rec, String resultKey) {
        Mono<List<LabResult>> existing = resultKey == null
                ? Mono.just(List.of())
                : labResultRepository.findAllByResultKey(resultKey).collectList();

        return existing.flatMap(versions -> {
            int nextVersion = versions.stream().mapToInt(v -> v.getVersion() == null ? 1 : v.getVersion()).max().orElse(0) + 1;
            UUID supersedes = versions.stream()
                    .filter(v -> Boolean.TRUE.equals(v.getIsCurrent()))
                    .map(LabResult::getId)
                    .findFirst()
                    .orElse(null);

            LabResult result = build(m, rawMessage, rec, resultKey, nextVersion, supersedes);
            return labResultRepository.save(result)
                    .flatMap(saved -> supersedePrevious(resultKey, saved.getId())
                            .then(saveObservations(saved, m))
                            .then(enqueueAck(m, saved))
                            .thenReturn(saved))
                    .doOnSuccess(saved -> log.info("Recorded lab result '{}' v{} ({}, {}, kind={}, current)",
                            saved.getMessageControlId(), saved.getVersion(), saved.getReconciliationStatus(),
                            saved.getMatchMethod(), saved.getMessageKind()));
        });
    }

    /**
     * Enqueue the HL7 ACK^R01 for this result to the outbox (relayed to the
     * lab-result-ack topic). Enqueued in the same transaction as the result, so an
     * acknowledgement is emitted iff the result was durably recorded.
     */
    private Mono<Void> enqueueAck(LabResultMessage m, LabResult saved) {
        String ackHl7 = ackBuilder.buildAccept(m);
        return domainEventPublisher.publish(ACK_EVENT_TYPE, ackHl7, null, saved.getCorrelationId());
    }

    private Mono<Void> supersedePrevious(String resultKey, UUID currentId) {
        if (resultKey == null) {
            return Mono.empty();
        }
        Query priors = Query.query(Criteria.where("resultKey").is(resultKey)
                .and("isCurrent").is(true)
                .and("id").not(currentId));
        Update update = Update.update("isCurrent", false)
                .set("supersededBy", currentId)
                .set("supersededAt", OffsetDateTime.now());
        return template.update(priors, update, LabResult.class)
                .doOnNext(n -> {
                    if (n > 0) {
                        log.info("Superseded {} prior version(s) of result key '{}'", n, resultKey);
                    }
                })
                .then();
    }

    private LabResult build(LabResultMessage m, String rawMessage, Reconciliation rec, String resultKey,
                            int version, UUID supersedes) {
        boolean forwardable = rec.reconciled() && m.hasMeaningfulObservations();
        // Unsolicited results with real observations wait for a clinician to accept/reject.
        String reviewStatus = (!rec.reconciled() && m.hasMeaningfulObservations()) ? "PENDING_REVIEW" : "NONE";
        return LabResult.builder()
                .reviewStatus(reviewStatus)
                .messageControlId(m.getMessageControlId())
                .placerOrderNumber(m.getPlacerOrderNumber())
                .fillerOrderNumber(m.getFillerOrderNumber())
                .labCode(m.getLabCode())
                .orderingMflCode(m.getOrderingMflCode())
                .orderingHmisCode(m.getOrderingHmisCode())
                .patientIdentifier(m.getPatientIdentifier())
                .patientName(m.getPatientName())
                .patientDob(m.getPatientDob())
                .patientSex(m.getPatientSex())
                .testLoinc(m.getTestLoinc())
                .testName(m.getTestName())
                .orderControl(m.getOrderControl())
                .orderStatusCode(m.getOrderStatusCode())
                .resultStatus(m.getResultStatus())
                .messageKind(m.messageKind())
                .specimenCollectedAt(m.getSpecimenCollectedAt())
                .specimenReceivedAt(m.getSpecimenReceivedAt())
                .resultKey(resultKey)
                .version(version)
                .isCurrent(true)
                .supersedes(supersedes)
                .reconciliationStatus(rec.status())
                .matchMethod(rec.method())
                .candidateCount(rec.candidateCount())
                .orderStatusId(rec.order() != null ? rec.order().getId() : null)
                .correlationId(rec.order() != null ? rec.order().getCorrelationId() : null)
                .forwardStatus(forwardable ? "PENDING" : "NOT_APPLICABLE")
                .forwardAttempts((short) 0)
                .rawMessage(rawMessage)
                .receivedAt(OffsetDateTime.now())
                .build();
    }

    private Mono<Void> saveObservations(LabResult saved, LabResultMessage m) {
        if (m.getObservations() == null || m.getObservations().isEmpty()) {
            return Mono.empty();
        }
        Flux<LabResultObservation> entities = Flux.fromIterable(m.getObservations()).map(o -> LabResultObservation.builder()
                .labResultId(saved.getId())
                .setId(o.getSetId())
                .valueType(o.getValueType())
                .observationLoinc(o.getLoinc())
                .observationLocalCode(o.getLocalCode())
                .observationText(o.getText())
                .value(o.getValue())
                .numericValue(o.getNumericValue())
                .units(o.getUnits())
                .referenceRange(o.getReferenceRange())
                .abnormalFlags(o.getAbnormalFlags())
                .observationStatus(o.getStatus())
                .observedAt(o.getObservedAt())
                .createdAt(OffsetDateTime.now())
                .build());
        return observationRepository.saveAll(entities).then();
    }

    // ---- post-persist side effects --------------------------------------------

    private Mono<Void> afterPersist(LabResult saved, LabResultMessage m, Reconciliation rec) {
        Mono<Void> captureFiller = captureFiller(m, rec);
        Mono<Void> forward = "PENDING".equals(saved.getForwardStatus()) ? forwarder.forward(saved) : Mono.empty();
        Mono<Void> notify;
        if (rec.reconciled() && m.hasMeaningfulObservations()) {
            notify = notify(saved, rec);
        } else if ("PENDING_REVIEW".equals(saved.getReviewStatus())) {
            notify = notifyReviewRequired(saved);
        } else {
            notify = Mono.empty();
        }
        // Best-effort: the result is already committed and its ACK enqueued, so a
        // side-effect failure here must not surface as a processing error (which would
        // otherwise trigger a spurious negative ACK).
        return captureFiller.then(forward).then(notify)
                .onErrorResume(ex -> {
                    log.warn("Post-persist side effect failed for lab result '{}': {}",
                            saved.getMessageControlId(), ex.getMessage());
                    return Mono.empty();
                });
    }

    /** Tell the facility an unsolicited result is awaiting a clinician's accept/reject. */
    private Mono<Void> notifyReviewRequired(LabResult result) {
        return resolveFacility(result.getOrderingMflCode())
                .flatMap(facility -> {
                    String testLabel = result.getTestName() != null ? result.getTestName() : result.getTestLoinc();
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("labResultId", result.getId().toString());
                    data.put("reconciliationStatus", result.getReconciliationStatus());
                    data.put("candidateCount", result.getCandidateCount());
                    data.put("testLoinc", result.getTestLoinc());
                    data.put("patientIdentifier", result.getPatientIdentifier());
                    NotificationDTO.DispatchRequest request = NotificationDTO.DispatchRequest.builder()
                            .type(NOTIFICATION_TYPE)
                            .title("Unsolicited lab result — review required")
                            .body("An unmatched result" + (testLabel != null ? " for " + testLabel : "")
                                    + (result.getPatientName() != null ? " (" + result.getPatientName() + ")" : "")
                                    + " needs review before it can be accepted")
                            .data(toJson(data))
                            .build();
                    return notificationDispatcher.dispatchToFacility(facility.getId(), request).then();
                })
                .switchIfEmpty(Mono.<Void>fromRunnable(() -> log.warn(
                        "No facility for MFL '{}' — unsolicited lab result '{}' cannot be reviewed",
                        result.getOrderingMflCode(), result.getMessageControlId())));
    }

    /** Record the lab's accession number on the order the first time we see it. */
    private Mono<Void> captureFiller(LabResultMessage m, Reconciliation rec) {
        OrderStatus order = rec.order();
        if (order == null || isBlank(m.getFillerOrderNumber()) || !isBlank(order.getFillerOrderNumber())) {
            return Mono.empty();
        }
        order.setFillerOrderNumber(m.getFillerOrderNumber());
        return orderStatusRepository.save(order).then();
    }

    private Mono<Void> notify(LabResult result, Reconciliation rec) {
        return resolveFacility(result.getOrderingMflCode())
                .flatMap(facility -> notificationDispatcher
                        .dispatchToFacility(facility.getId(), buildNotification(result, rec)).then())
                .switchIfEmpty(Mono.<Void>fromRunnable(() -> log.warn(
                        "No facility for MFL '{}' — lab result '{}' not notified",
                        result.getOrderingMflCode(), result.getMessageControlId())));
    }

    private Mono<Facility> resolveFacility(String mflCode) {
        if (isBlank(mflCode)) {
            return Mono.empty();
        }
        return facilityService.cachedAll()
                .flatMap(facilities -> Mono.justOrEmpty(facilities.stream()
                        .filter(f -> Boolean.TRUE.equals(f.getIsActive()))
                        .filter(f -> mflCode.equals(f.getMflCode()))
                        .findFirst()));
    }

    private NotificationDTO.DispatchRequest buildNotification(LabResult r, Reconciliation rec) {
        String testLabel = r.getTestName() != null ? r.getTestName() : r.getTestLoinc();
        boolean corrected = "C".equalsIgnoreCase(r.getResultStatus());
        boolean update = !corrected && r.getVersion() != null && r.getVersion() > 1;
        String title = corrected ? "Lab result corrected" : update ? "Lab result updated" : "Lab result received";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("labResultId", r.getId().toString());
        data.put("orderId", rec.order() != null ? rec.order().getOrderId() : null);
        data.put("testLoinc", r.getTestLoinc());
        data.put("resultStatus", r.getResultStatus());
        data.put("version", r.getVersion());
        data.put("patientIdentifier", r.getPatientIdentifier());

        return NotificationDTO.DispatchRequest.builder()
                .type(NOTIFICATION_TYPE)
                .title(title)
                .body(testLabel != null
                        ? title + " for " + testLabel + (r.getPatientName() != null ? " (" + r.getPatientName() + ")" : "")
                        : title)
                .data(toJson(data))
                .correlationId(rec.order() != null ? rec.order().getCorrelationId() : null)
                .build();
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** True only for the message-control-id uniqueness violation (an idempotent re-delivery). */
    private static boolean isDuplicate(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return message != null && message.contains("uq_lab_result_message_control");
    }

    /** Outcome of matching a result to an order. */
    private record Reconciliation(String status, String method, OrderStatus order, int candidateCount) {
        static Reconciliation matched(String method, OrderStatus order) {
            return new Reconciliation("RECONCILED", method, order, 1);
        }

        static Reconciliation unsolicited(int candidateCount) {
            return new Reconciliation("UNSOLICITED", "NONE", null, candidateCount);
        }

        boolean reconciled() {
            return "RECONCILED".equals(status);
        }
    }
}
