package moh.gov.zm.lis.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.redis.lock.DistributedLockService;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Weekly housekeeping that prunes the messaging tables every Saturday at 00:01 UTC,
 * keeping the most recent {@value #RETENTION_DAYS} days. The distributed lock ensures
 * exactly one instance runs the purge even with several replicas.
 *
 * <p>Only rows that are safe to remove are deleted, so nothing in flight is lost:
 * <ul>
 *   <li>{@code outbound_event_outbox}: published rows whose {@code published_at} is
 *       older than the retention window. Unpublished (pending / failed) rows are kept
 *       so the relay can still send them.</li>
 *   <li>{@code inbound_event_log}: completed rows (a non-null {@code processed_at})
 *       older than the retention window. In-flight and FAILED rows (no
 *       {@code processed_at}) are kept for reprocessing / investigation.</li>
 * </ul>
 * Recent idempotency records stay within the window, and the Redis idempotency cache
 * (24h TTL) still guards the most recent ones.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagingLogPurgeScheduler {
    /** How much history to retain. */
    private static final int RETENTION_DAYS = 7;
    /** Lock held for the duration of the purge; generous relative to two DELETEs. */
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);

    // RETENTION_DAYS is a compile-time int constant (no injection risk).
    private static final String DELETE_PUBLISHED_OUTBOX = """
            DELETE FROM messaging.outbound_event_outbox
            WHERE is_published = TRUE
              AND published_at < now() - interval '%d days'
            """.formatted(RETENTION_DAYS);

    private static final String DELETE_PROCESSED_INBOUND = """
            DELETE FROM messaging.inbound_event_log
            WHERE processed_at IS NOT NULL
              AND processed_at < now() - interval '%d days'
            """.formatted(RETENTION_DAYS);

    private final DatabaseClient databaseClient;
    private final DistributedLockService lockService;

    // cron fields: second minute hour day-of-month month day-of-week  ->  00:01:00 every Saturday (UTC)
    @Scheduled(cron = "0 1 0 * * SAT", zone = "UTC")
    public void purge() {
        lockService.withLock(DistributedLockService.MESSAGING_PURGE_LOCK, LOCK_TTL, purgeAll()).block();
    }

    private Mono<Void> purgeAll() {
        return delete(DELETE_PUBLISHED_OUTBOX)
                .flatMap(outboxDeleted -> delete(DELETE_PROCESSED_INBOUND)
                        .doOnNext(inboundDeleted -> log.warn(
                                "Weekly messaging purge complete (retained last {} days) — deleted {} published outbox row(s) and {} processed inbound_event_log row(s)",
                                RETENTION_DAYS, outboxDeleted, inboundDeleted)))
                .then();
    }

    private Mono<Long> delete(String sql) {
        return databaseClient.sql(sql).fetch().rowsUpdated();
    }
}
