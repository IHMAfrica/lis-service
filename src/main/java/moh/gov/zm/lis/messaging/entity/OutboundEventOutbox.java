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
@Table(schema = "messaging", name = "outbound_event_outbox")
public class OutboundEventOutbox {
    @Id
    @Column("id")
    private UUID id;

    @Column("event_type_id")
    private Short eventTypeId;

    @Column("topic")
    private String topic;

    @Column("payload")
    private String payload;

    @Column("correlation_id")
    private UUID correlationId;

    @Column("is_published")
    private Boolean isPublished;

    @Column("published_at")
    private OffsetDateTime publishedAt;

    @Column("retry_count")
    private Short retryCount;

    @Column("last_error")
    private String lastError;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("created_by")
    private UUID createdBy;
}
