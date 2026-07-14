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
@Table(schema = "lab", name = "order_ack_status")
public class OrderAckStatus {
    @Id
    @Column("id")
    private UUID id;

    @Column("message_control_id")
    private String messageControlId;

    @Column("order_ack_date")
    private LocalDate orderAckDate;

    @Column("order_ack_time")
    private LocalTime orderAckTime;

    @Column("sending_facility_lab_code")
    private String sendingFacilityLabCode;

    @Column("receiving_facility_mfl_code")
    private String receivingFacilityMflCode;

    @Column("ack_code")
    private String ackCode;

    @Column("ref_message_control_id")
    private String refMessageControlId;

    @Column("text_message")
    private String textMessage;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
