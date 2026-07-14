package moh.gov.zm.lis.lab.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.common.PageMapper;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.disa.forward.LabResultForwarder;
import moh.gov.zm.lis.exception.ConflictException;
import moh.gov.zm.lis.exception.ForbiddenException;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.iam.repository.UserFacilityRepository;
import moh.gov.zm.lis.lab.dto.LabResultDTO;
import moh.gov.zm.lis.lab.entity.LabResult;
import moh.gov.zm.lis.lab.entity.LabResultObservation;
import moh.gov.zm.lis.lab.repository.LabResultObservationRepository;
import moh.gov.zm.lis.lab.repository.LabResultRepository;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import moh.gov.zm.lis.notify.service.NotificationDispatcher;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Clinician review of unsolicited lab results. A clinician at the ordering facility
 * accepts (result is then forwarded downstream) or rejects (never forwarded); either
 * way the other users at that facility are notified of the decision.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabResultReviewService {
    private static final String NOTIFICATION_TYPE = "LAB_RESULT";

    private final LabResultRepository labResultRepository;
    private final LabResultObservationRepository observationRepository;
    private final UserFacilityRepository userFacilityRepository;
    private final FacilityService facilityService;
    private final NotificationDispatcher notificationDispatcher;
    private final LabResultForwarder forwarder;
    private final R2dbcEntityTemplate template;
    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;

    /** Unsolicited results awaiting review at the calling clinician's facilities. */
    public Mono<PagedResponse<LabResultDTO.LabResultResponse>> pendingReview(UUID userId, int page, int size) {
        return facilityMflCodes(userId).collectList().flatMap(mflCodes -> {
            if (mflCodes.isEmpty()) {
                return Mono.just(PageMapper.of(List.of(), 0, page, size));
            }
            Criteria criteria = Criteria.where("reviewStatus").is("PENDING_REVIEW")
                    .and("isCurrent").is(true)
                    .and("orderingMflCode").in(mflCodes);
            Query paged = Query.query(criteria).sort(Sort.by(Sort.Direction.DESC, "receivedAt"))
                    .with(PageRequest.of(page, size));
            Mono<List<LabResultDTO.LabResultResponse>> content = template.select(paged, LabResult.class)
                    .map(r -> toResponse(r, null))
                    .collectList();
            Mono<Long> total = template.count(Query.query(criteria), LabResult.class);
            return Mono.zip(content, total).map(t -> PageMapper.of(t.getT1(), t.getT2(), page, size));
        });
    }

    public Mono<LabResultDTO.LabResultResponse> findById(UUID id) {
        return labResultRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LabResult", String.valueOf(id))))
                .flatMap(this::withObservations);
    }

    public Mono<LabResultDTO.LabResultResponse> accept(UUID id, UUID userId) {
        return reviewable(id, userId).flatMap(ctx -> {
            LabResult r = ctx.result();
            r.setReviewStatus("ACCEPTED");
            r.setReviewedBy(userId);
            r.setReviewedAt(OffsetDateTime.now());
            r.setForwardStatus("PENDING"); // persisted before forwarding, so a crash leaves it retryable
            return labResultRepository.save(r)
                    .flatMap(saved -> forwarder.forward(saved)
                            .then(notifyDecision(ctx.facility(), saved, userId, true))
                            .then(withObservations(saved)));
        });
    }

    public Mono<LabResultDTO.LabResultResponse> reject(UUID id, UUID userId, String note) {
        return reviewable(id, userId).flatMap(ctx -> {
            LabResult r = ctx.result();
            r.setReviewStatus("REJECTED");
            r.setReviewedBy(userId);
            r.setReviewedAt(OffsetDateTime.now());
            r.setReviewNote(note);
            return labResultRepository.save(r)
                    .flatMap(saved -> notifyDecision(ctx.facility(), saved, userId, false)
                            .then(withObservations(saved)));
        });
    }

    // ---- helpers ---------------------------------------------------------------

    private Mono<ReviewContext> reviewable(UUID id, UUID userId) {
        return labResultRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LabResult", String.valueOf(id))))
                .flatMap(result -> {
                    if (!"PENDING_REVIEW".equals(result.getReviewStatus())) {
                        return Mono.error(new ConflictException(
                                "Lab result is not pending review (status: " + result.getReviewStatus() + ")"));
                    }
                    return resolveFacility(result.getOrderingMflCode())
                            .switchIfEmpty(Mono.error(new ConflictException(
                                    "No facility resolves for MFL code '" + result.getOrderingMflCode() + "'; cannot review")))
                            .flatMap(facility -> userFacilityRepository
                                    .existsByUserIdAndFacilityIdAndIsActiveTrue(userId, facility.getId())
                                    .flatMap(member -> member
                                            ? Mono.just(new ReviewContext(result, facility))
                                            : Mono.error(new ForbiddenException(
                                            "User is not assigned to the facility for this result"))));
                });
    }

    private Mono<Void> notifyDecision(Facility facility, LabResult result, UUID actingUserId, boolean accepted) {
        String testLabel = result.getTestName() != null ? result.getTestName() : result.getTestLoinc();
        String verb = accepted ? "accepted" : "rejected";
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("labResultId", result.getId().toString());
        data.put("decision", accepted ? "ACCEPTED" : "REJECTED");
        data.put("reviewedBy", actingUserId.toString());
        data.put("forwarded", accepted);
        data.put("testLoinc", result.getTestLoinc());
        data.put("patientIdentifier", result.getPatientIdentifier());

        NotificationDTO.DispatchRequest request = NotificationDTO.DispatchRequest.builder()
                .type(NOTIFICATION_TYPE)
                .title("Unsolicited lab result " + verb)
                .body("An unsolicited result" + (testLabel != null ? " for " + testLabel : "")
                        + (result.getPatientName() != null ? " (" + result.getPatientName() + ")" : "")
                        + " was " + verb + (accepted ? " and forwarded" : ""))
                .data(toJson(data))
                .correlationId(result.getCorrelationId())
                .build();

        // notify the other users at the facility (exclude the clinician who acted)
        return notificationDispatcher.dispatchToFacility(facility.getId(), request, actingUserId).then();
    }

    private Mono<Facility> resolveFacility(String mflCode) {
        if (mflCode == null || mflCode.isBlank()) {
            return Mono.empty();
        }
        return facilityService.cachedAll()
                .flatMap(facilities -> Mono.justOrEmpty(facilities.stream()
                        .filter(f -> Boolean.TRUE.equals(f.getIsActive()))
                        .filter(f -> mflCode.equals(f.getMflCode()))
                        .findFirst()));
    }

    private reactor.core.publisher.Flux<String> facilityMflCodes(UUID userId) {
        return databaseClient.sql("""
                        SELECT f.mfl_code
                        FROM iam.user_facility uf
                        JOIN ref.facility f ON f.id = uf.facility_id
                        WHERE uf.user_id = :userId AND uf.is_active AND f.mfl_code IS NOT NULL
                        """)
                .bind("userId", userId)
                .map(row -> row.get("mfl_code", String.class))
                .all();
    }

    private Mono<LabResultDTO.LabResultResponse> withObservations(LabResult result) {
        return observationRepository.findAllByLabResultIdOrderBySetId(result.getId())
                .map(this::toObservation)
                .collectList()
                .map(obs -> toResponse(result, obs));
    }

    private LabResultDTO.LabResultResponse toResponse(LabResult r, List<LabResultDTO.ObservationResponse> observations) {
        return LabResultDTO.LabResultResponse.builder()
                .id(r.getId())
                .messageControlId(r.getMessageControlId())
                .placerOrderNumber(r.getPlacerOrderNumber())
                .fillerOrderNumber(r.getFillerOrderNumber())
                .labCode(r.getLabCode())
                .orderingMflCode(r.getOrderingMflCode())
                .orderingHmisCode(r.getOrderingHmisCode())
                .patientIdentifier(r.getPatientIdentifier())
                .patientName(r.getPatientName())
                .patientDob(r.getPatientDob())
                .patientSex(r.getPatientSex())
                .testLoinc(r.getTestLoinc())
                .testName(r.getTestName())
                .resultStatus(r.getResultStatus())
                .messageKind(r.getMessageKind())
                .specimenCollectedAt(r.getSpecimenCollectedAt())
                .reconciliationStatus(r.getReconciliationStatus())
                .matchMethod(r.getMatchMethod())
                .candidateCount(r.getCandidateCount())
                .reviewStatus(r.getReviewStatus())
                .reviewedBy(r.getReviewedBy())
                .reviewedAt(r.getReviewedAt())
                .reviewNote(r.getReviewNote())
                .version(r.getVersion())
                .isCurrent(r.getIsCurrent())
                .forwardStatus(r.getForwardStatus())
                .receivedAt(r.getReceivedAt())
                .observations(observations)
                .build();
    }

    private LabResultDTO.ObservationResponse toObservation(LabResultObservation o) {
        return LabResultDTO.ObservationResponse.builder()
                .setId(o.getSetId())
                .valueType(o.getValueType())
                .loinc(o.getObservationLoinc())
                .localCode(o.getObservationLocalCode())
                .text(o.getObservationText())
                .value(o.getValue())
                .numericValue(o.getNumericValue())
                .units(o.getUnits())
                .referenceRange(o.getReferenceRange())
                .abnormalFlags(o.getAbnormalFlags())
                .status(o.getObservationStatus())
                .observedAt(o.getObservedAt())
                .build();
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return null;
        }
    }

    private record ReviewContext(LabResult result, Facility facility) {
    }
}
