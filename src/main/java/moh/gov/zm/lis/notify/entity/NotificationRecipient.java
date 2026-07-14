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
@Table(schema = "notify", name = "notification_recipient")
public class NotificationRecipient {
    @Id
    @Column("id")
    private UUID id;

    @Column("notification_id")
    private UUID notificationId;

    @Column("user_id")
    private UUID userId;

    @Column("read_at")
    private OffsetDateTime readAt;

    @Column("deleted_at")
    private OffsetDateTime deletedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
