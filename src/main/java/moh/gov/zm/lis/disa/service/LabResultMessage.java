package moh.gov.zm.lis.disa.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Parsed HL7 ORU^R01 lab result. {@link #messageKind()} distinguishes a clinical
 * result (has at least one meaningful observation) from an order-status / monitoring
 * update (ORC only, no OBX).
 */
@Data
@Builder
public class LabResultMessage {
    public static final String KIND_RESULT = "RESULT";
    public static final String KIND_STATUS_UPDATE = "STATUS_UPDATE";

    private String messageControlId;
    private OffsetDateTime messageDateTime;
    private String labCode;
    private String sendingFacilityName;
    private String orderingMflCode;
    private String orderingHmisCode;
    private String patientIdentifier;
    private String patientName;
    private LocalDate patientDob;
    private String patientSex;
    private String placerOrderNumber;
    private String fillerOrderNumber;
    private String orderControl;
    private String orderStatusCode;
    private String testLoinc;
    private String testName;
    private String resultStatus;
    private OffsetDateTime specimenCollectedAt;
    private OffsetDateTime specimenReceivedAt;
    private String priority;
    private List<Observation> observations;

    /** A message is a RESULT when it carries at least one identifiable, valued observation. */
    public String messageKind() {
        boolean meaningful = observations != null && observations.stream().anyMatch(Observation::isMeaningful);
        return meaningful ? KIND_RESULT : KIND_STATUS_UPDATE;
    }

    public boolean hasMeaningfulObservations() {
        return KIND_RESULT.equals(messageKind());
    }

    @Data
    @Builder
    public static class Observation {
        private Integer setId;
        private String valueType;
        private String loinc;
        private String localCode;
        private String text;
        private String value;
        private BigDecimal numericValue;
        private String units;
        private String referenceRange;
        private String abnormalFlags;
        private String status;
        private OffsetDateTime observedAt;

        /** Has both an identifier (code or label) and a value — i.e. a real observation, not a header row. */
        public boolean isMeaningful() {
            boolean hasId = notBlank(loinc) || notBlank(localCode) || notBlank(text);
            return hasId && notBlank(value);
        }

        private static boolean notBlank(String s) {
            return s != null && !s.isBlank();
        }
    }
}
