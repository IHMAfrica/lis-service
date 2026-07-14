package moh.gov.zm.lis.lab.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "lab", name = "order_status")
public class OrderStatus {
    @Id
    @Column("id")
    private UUID id;

    @Column("message_control_id")
    private String messageControlId;

    @Column("order_id")
    private String orderId;

    @Column("order_date")
    private LocalDate orderDate;

    @Column("order_time")
    private LocalTime orderTime;

    @Column("mfl_code")
    private String mflCode;

    @Column("lab_code")
    private String labCode;

    @Column("patient_identifier")
    private String patientIdentifier;

    @Column("test_loinc")
    private String testLoinc;

    @Column("filler_order_number")
    private String fillerOrderNumber;

    @Column("correlation_id")
    private UUID correlationId;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
