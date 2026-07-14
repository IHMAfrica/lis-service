package moh.gov.zm.lis.lab.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "lab", name = "lab_result_observation")
public class LabResultObservation {
    @Id
    @Column("id")
    private UUID id;

    @Column("lab_result_id")
    private UUID labResultId;

    @Column("set_id")
    private Integer setId;

    @Column("value_type")
    private String valueType;

    @Column("observation_loinc")
    private String observationLoinc;

    @Column("observation_local_code")
    private String observationLocalCode;

    @Column("observation_text")
    private String observationText;

    @Column("value")
    private String value;

    @Column("numeric_value")
    private BigDecimal numericValue;

    @Column("units")
    private String units;

    @Column("reference_range")
    private String referenceRange;

    @Column("abnormal_flags")
    private String abnormalFlags;

    @Column("observation_status")
    private String observationStatus;

    @Column("observed_at")
    private OffsetDateTime observedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
