package moh.gov.zm.lis.notify.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PageMapper;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Per-user notification management (the recipient's inbox): list, unread count,
 * mark read, mark all read and (soft) delete. All operations are scoped to the
 * calling user and read/write {@code notify.notification_recipient} joined with
 * {@code notify.notification}.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final DatabaseClient db;

    public Mono<PagedResponse<NotificationDTO.NotificationResponse>> list(
            UUID userId, boolean unreadOnly, int page, int size) {
        String filter = "WHERE nr.user_id = :userId AND nr.deleted_at IS NULL"
                + (unreadOnly ? " AND nr.read_at IS NULL" : "");

        String selectSql = """
                SELECT n.id, n.type, n.title, n.body, n.data, n.facility_id, n.correlation_id,
                       n.created_at, nr.read_at
                FROM notify.notification_recipient nr
                JOIN notify.notification n ON n.id = nr.notification_id
                """ + filter + " ORDER BY nr.created_at DESC LIMIT :size OFFSET :offset";

        Mono<java.util.List<NotificationDTO.NotificationResponse>> content = db.sql(selectSql)
                .bind("userId", userId)
                .bind("size", size)
                .bind("offset", (long) page * size)
                .map(row -> NotificationDTO.NotificationResponse.builder()
                        .id(row.get("id", UUID.class))
                        .type(row.get("type", String.class))
                        .title(row.get("title", String.class))
                        .body(row.get("body", String.class))
                        .data(row.get("data", String.class))
                        .facilityId(row.get("facility_id", Long.class))
                        .correlationId(row.get("correlation_id", UUID.class))
                        .createdAt(row.get("created_at", OffsetDateTime.class))
                        .readAt(row.get("read_at", OffsetDateTime.class))
                        .build())
                .all()
                .collectList();

        Mono<Long> total = db.sql("SELECT count(*) FROM notify.notification_recipient nr " + filter)
                .bind("userId", userId)
                .map(row -> row.get(0, Long.class))
                .one();

        return Mono.zip(content, total)
                .map(t -> PageMapper.of(t.getT1(), t.getT2(), page, size));
    }

    public Mono<NotificationDTO.UnreadCountResponse> unreadCount(UUID userId) {
        return db.sql("""
                        SELECT count(*) FROM notify.notification_recipient
                        WHERE user_id = :userId AND deleted_at IS NULL AND read_at IS NULL
                        """)
                .bind("userId", userId)
                .map(row -> row.get(0, Long.class))
                .one()
                .map(count -> NotificationDTO.UnreadCountResponse.builder().unread(count).build());
    }

    /** Idempotent: marking an already-read or unknown notification is a no-op. */
    public Mono<Void> markRead(UUID userId, UUID notificationId) {
        return db.sql("""
                        UPDATE notify.notification_recipient SET read_at = now()
                        WHERE notification_id = :nid AND user_id = :userId
                          AND read_at IS NULL AND deleted_at IS NULL
                        """)
                .bind("nid", notificationId)
                .bind("userId", userId)
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<NotificationDTO.MarkReadResult> markAllRead(UUID userId) {
        return db.sql("""
                        UPDATE notify.notification_recipient SET read_at = now()
                        WHERE user_id = :userId AND read_at IS NULL AND deleted_at IS NULL
                        """)
                .bind("userId", userId)
                .fetch()
                .rowsUpdated()
                .map(updated -> NotificationDTO.MarkReadResult.builder().updated(updated).build());
    }

    /** Soft delete: the user's copy is hidden but the notification is retained. */
    public Mono<Void> delete(UUID userId, UUID notificationId) {
        return db.sql("""
                        UPDATE notify.notification_recipient SET deleted_at = now()
                        WHERE notification_id = :nid AND user_id = :userId AND deleted_at IS NULL
                        """)
                .bind("nid", notificationId)
                .bind("userId", userId)
                .fetch()
                .rowsUpdated()
                .then();
    }
}
