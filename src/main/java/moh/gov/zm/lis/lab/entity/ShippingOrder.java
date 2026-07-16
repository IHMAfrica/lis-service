package moh.gov.zm.lis.lab.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Per-order data captured at ingestion so the facility shipping list can be
 * produced later. Populated from the DISA payload alongside the {@link OrderStatus}
 * row (see {@code DisaLabOrderService}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "lab", name = "shipping_order")
public class ShippingOrder {
    @Id
    @Column("id")
    private UUID id;

    @Column("order_id")
    private String orderId;

    @Column("correlation_id")
    private UUID correlationId;

    @Column("mfl_code")
    private String mflCode;

    @Column("lab_code")
    private String labCode;

    @Column("full_name")
    private String fullName;

    @Column("age")
    private Integer age;

    @Column("sex")
    private String sex;

    @Column("test_type")
    private String testType;

    @Column("requested_date")
    private String requestedDate;

    @Column("specimen_collected_date")
    private String specimenCollectedDate;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
