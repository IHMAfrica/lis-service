package moh.gov.zm.lis.disa.forward;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.lab.entity.LabResult;
import moh.gov.zm.lis.lab.entity.LabResultObservation;
import moh.gov.zm.lis.lab.entity.OrderStatus;
import moh.gov.zm.lis.lab.repository.LabResultObservationRepository;
import moh.gov.zm.lis.lab.repository.LabResultRepository;
import moh.gov.zm.lis.lab.repository.OrderStatusRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import zm.gov.moh.zmscpromessagereceiver.grpc.MessageRequest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LabResultForwarder {
    private static final String SOURCE_SYSTEM = "disa";
    private static final String MESSAGE_TYPE = "lab result";

    private final MessageReceiverClient client;
    private final LabResultRepository labResultRepository;
    private final LabResultObservationRepository observationRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ObjectMapper objectMapper;

    public Mono<Void> forward(LabResult result) {
        return buildRequest(result)
                .flatMap(request -> client.send(request)
                        .then(markSent(result))
                        .onErrorResume(ex -> {
                            log.warn("Failed to forward lab result '{}' to downstream: {}",
                                    result.getMessageControlId(), ex.getMessage());
                            return markFailed(result, ex.getMessage());
                        }));
    }

    private Mono<MessageRequest> buildRequest(LabResult result) {
        Mono<List<LabResultObservation>> observations =
                observationRepository.findAllByLabResultIdOrderBySetId(result.getId()).collectList();
        Mono<Optional<OrderStatus>> order = result.getOrderStatusId() == null
                ? Mono.just(Optional.empty())
                : orderStatusRepository.findById(result.getOrderStatusId()).map(Optional::of).defaultIfEmpty(Optional.empty());

        return Mono.zip(observations, order).map(t -> {
            String payload = toJson(payload(result, t.getT1(), t.getT2().orElse(null)));
            return MessageRequest.newBuilder()
                    .setMessageId(nz(result.getMessageControlId()))
                    .setPayload(payload)
                    .setSourceSystem(SOURCE_SYSTEM)
                    .setMessageType(MESSAGE_TYPE)
                    .setStatus(result.getResultStatus() != null ? result.getResultStatus() : "F")
                    .build();
        });
    }

    /** Comprehensive payload so the receiver can link the result to its order and update its record. */
    private Map<String, Object> payload(LabResult r, List<LabResultObservation> obs, OrderStatus order) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("labResultId", str(r.getId()));
        root.put("messageControlId", r.getMessageControlId());
        root.put("correlationId", str(r.getCorrelationId()));
        root.put("resultStatus", r.getResultStatus());
        root.put("orderControl", r.getOrderControl());
        root.put("orderStatusCode", r.getOrderStatusCode());
        root.put("placerOrderNumber", r.getPlacerOrderNumber());
        root.put("fillerOrderNumber", r.getFillerOrderNumber());

        Map<String, Object> version = new LinkedHashMap<>();
        version.put("version", r.getVersion());
        version.put("isCurrent", r.getIsCurrent());
        version.put("correction", "C".equalsIgnoreCase(r.getResultStatus()));
        version.put("supersedesLabResultId", str(r.getSupersedes()));
        root.put("versioning", version);

        Map<String, Object> reconciliation = new LinkedHashMap<>();
        reconciliation.put("status", r.getReconciliationStatus());
        reconciliation.put("method", r.getMatchMethod());
        root.put("reconciliation", reconciliation);

        Map<String, Object> review = new LinkedHashMap<>();
        review.put("status", r.getReviewStatus());
        review.put("reviewedBy", str(r.getReviewedBy()));
        review.put("reviewedAt", str(r.getReviewedAt()));
        root.put("review", review);

        if (order != null) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("orderId", order.getOrderId());
            o.put("orderMessageControlId", order.getMessageControlId());
            o.put("correlationId", str(order.getCorrelationId()));
            o.put("labCode", order.getLabCode());
            o.put("mflCode", order.getMflCode());
            root.put("order", o);
        }

        Map<String, Object> lab = new LinkedHashMap<>();
        lab.put("code", r.getLabCode());
        root.put("lab", lab);

        Map<String, Object> facility = new LinkedHashMap<>();
        facility.put("mflCode", r.getOrderingMflCode());
        facility.put("hmisCode", r.getOrderingHmisCode());
        root.put("facility", facility);

        Map<String, Object> patient = new LinkedHashMap<>();
        patient.put("identifier", r.getPatientIdentifier());
        patient.put("name", r.getPatientName());
        patient.put("dateOfBirth", str(r.getPatientDob()));
        patient.put("sex", r.getPatientSex());
        root.put("patient", patient);

        Map<String, Object> test = new LinkedHashMap<>();
        test.put("loinc", r.getTestLoinc());
        test.put("name", r.getTestName());
        root.put("test", test);

        root.put("specimenCollectedAt", str(r.getSpecimenCollectedAt()));
        root.put("specimenReceivedAt", str(r.getSpecimenReceivedAt()));
        root.put("reportedAt", str(r.getReceivedAt()));

        List<Map<String, Object>> observations = new ArrayList<>();
        for (LabResultObservation o : obs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("setId", o.getSetId());
            m.put("valueType", o.getValueType());
            m.put("loinc", o.getObservationLoinc());
            m.put("localCode", o.getObservationLocalCode());
            m.put("text", o.getObservationText());
            m.put("value", o.getValue());
            m.put("numericValue", o.getNumericValue());
            m.put("units", o.getUnits());
            m.put("referenceRange", o.getReferenceRange());
            m.put("abnormalFlags", o.getAbnormalFlags());
            m.put("status", o.getObservationStatus());
            m.put("observedAt", str(o.getObservedAt()));
            observations.add(m);
        }
        root.put("observations", observations);
        return root;
    }

    private Mono<Void> markSent(LabResult result) {
        result.setForwardStatus("SENT");
        result.setForwardedAt(OffsetDateTime.now());
        result.setForwardError(null);
        result.setForwardAttempts((short) (nz(result.getForwardAttempts()) + 1));
        return labResultRepository.save(result)
                .doOnSuccess(r -> {
                    assert r != null;
                    log.info("Forwarded lab result '{}' to downstream", r.getMessageControlId());
                })
                .then();
    }

    private Mono<Void> markFailed(LabResult result, String error) {
        result.setForwardStatus("FAILED");
        result.setForwardError(error);
        result.setForwardAttempts((short) (nz(result.getForwardAttempts()) + 1));
        return labResultRepository.save(result).then();
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to serialize forward payload for '{}': {}", data.get("messageControlId"), e.getMessage());
            return "{}";
        }
    }

    private static int nz(Short v) {
        return v == null ? 0 : v;
    }

    private static String nz(String v) {
        return v == null ? "" : v;
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }
}
