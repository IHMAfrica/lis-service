package moh.gov.zm.lis.messaging.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "messaging", name = "inbound_event_log")
public class InboundEventLog {
    @Id
    @Column("id")
    private UUID id;

    @Column("message_id")
    private String messageId;

    @Column("topic")
    private String topic;

    @Column("event_type_id")
    private Short eventTypeId;

    @Column("event_type_code")
    private String eventTypeCode;

    @Column("source_service")
    private String sourceService;

    @Column("correlation_id")
    private UUID correlationId;

    @Column("payload")
    private String payload;

    @Column("processing_status")
    private String processingStatus;

    @Column("error_message")
    private String errorMessage;

    @Column("received_at")
    private OffsetDateTime receivedAt;

    @Column("processed_at")
    private OffsetDateTime processedAt;
}
