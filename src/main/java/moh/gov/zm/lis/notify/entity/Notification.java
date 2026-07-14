package moh.gov.zm.lis.notify.entity;

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
@Table(schema = "notify", name = "notification")
public class Notification {
    @Id
    @Column("id")
    private UUID id;

    @Column("type")
    private String type;

    @Column("title")
    private String title;

    @Column("body")
    private String body;

    @Column("data")
    private String data;

    @Column("facility_id")
    private Long facilityId;

    @Column("correlation_id")
    private UUID correlationId;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
