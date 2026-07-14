package moh.gov.zm.lis.lab.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "lab", name = "lab_result")
public class LabResult {
    @Id
    @Column("id")
    private UUID id;

    @Column("message_control_id")
    private String messageControlId;

    @Column("placer_order_number")
    private String placerOrderNumber;

    @Column("filler_order_number")
    private String fillerOrderNumber;

    @Column("lab_code")
    private String labCode;

    @Column("ordering_mfl_code")
    private String orderingMflCode;

    @Column("ordering_hmis_code")
    private String orderingHmisCode;

    @Column("patient_identifier")
    private String patientIdentifier;

    @Column("patient_name")
    private String patientName;

    @Column("patient_dob")
    private LocalDate patientDob;

    @Column("patient_sex")
    private String patientSex;

    @Column("test_loinc")
    private String testLoinc;

    @Column("test_name")
    private String testName;

    @Column("order_control")
    private String orderControl;

    @Column("order_status_code")
    private String orderStatusCode;

    @Column("result_status")
    private String resultStatus;

    @Column("message_kind")
    private String messageKind;

    @Column("specimen_collected_at")
    private OffsetDateTime specimenCollectedAt;

    @Column("specimen_received_at")
    private OffsetDateTime specimenReceivedAt;

    @Column("result_key")
    private String resultKey;

    @Column("version")
    private Integer version;

    @Column("is_current")
    private Boolean isCurrent;

    @Column("supersedes")
    private UUID supersedes;

    @Column("superseded_by")
    private UUID supersededBy;

    @Column("superseded_at")
    private OffsetDateTime supersededAt;

    @Column("review_status")
    private String reviewStatus;

    @Column("reviewed_by")
    private UUID reviewedBy;

    @Column("reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column("review_note")
    private String reviewNote;

    @Column("reconciliation_status")
    private String reconciliationStatus;

    @Column("match_method")
    private String matchMethod;

    @Column("candidate_count")
    private Integer candidateCount;

    @Column("order_status_id")
    private UUID orderStatusId;

    @Column("correlation_id")
    private UUID correlationId;

    @Column("forward_status")
    private String forwardStatus;

    @Column("forward_attempts")
    private Short forwardAttempts;

    @Column("forwarded_at")
    private OffsetDateTime forwardedAt;

    @Column("forward_error")
    private String forwardError;

    @Column("raw_message")
    private String rawMessage;

    @Column("received_at")
    private OffsetDateTime receivedAt;
}
