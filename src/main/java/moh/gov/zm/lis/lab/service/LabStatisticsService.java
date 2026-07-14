package moh.gov.zm.lis.lab.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.lab.dto.LabStatisticsDTO;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Per-facility lab-workflow statistics. Every metric is scoped by facility MFL
 * code and bounded by a [from, to] date window, and the whole set is computed in
 * a single database round-trip: the lab_result metrics come from one conditional
 * aggregation and the order / acknowledgement counts from scalar sub-queries,
 * each backed by a composite (facility, timestamp) index.
 */
@Service
@RequiredArgsConstructor
public class LabStatisticsService {
    /** Default window when the caller does not supply one. */
    private static final int DEFAULT_WINDOW_DAYS = 30;

    private static final String STATS_SQL = """
            SELECT
                (SELECT count(*) FROM lab.order_status os
                     WHERE os.mfl_code = :mfl
                       AND os.created_at >= :from AND os.created_at < :toExclusive)              AS orders_sent,
                (SELECT count(*) FROM lab.order_ack_status oa
                     WHERE oa.receiving_facility_mfl_code = :mfl
                       AND oa.created_at >= :from AND oa.created_at < :toExclusive)              AS orders_acknowledged,
                count(*)                                                    AS results_received,
                count(*) FILTER (WHERE lr.message_kind = 'RESULT')          AS results_valid,
                count(*) FILTER (WHERE lr.reconciliation_status = 'UNSOLICITED') AS results_unsolicited
            FROM lab.lab_result lr
            WHERE lr.ordering_mfl_code = :mfl
              AND lr.received_at >= :from AND lr.received_at < :toExclusive
            """;

    private final DatabaseClient databaseClient;
    private final FacilityService facilityService;

    public Mono<LabStatisticsDTO.StatisticsResponse> forFacility(String mflCode, LocalDate from, LocalDate to) {
        LocalDate effectiveTo = to != null ? to : LocalDate.now(ZoneOffset.UTC);
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(DEFAULT_WINDOW_DAYS - 1L);

        // Half-open [start-of-from, start-of-day-after-to) so the whole `to` day is included.
        OffsetDateTime fromTs = effectiveFrom.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toExclusiveTs = effectiveTo.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        Mono<LabStatisticsDTO.StatisticsResponse> counts = databaseClient.sql(STATS_SQL)
                .bind("mfl", mflCode)
                .bind("from", fromTs)
                .bind("toExclusive", toExclusiveTs)
                .map((row, meta) -> LabStatisticsDTO.StatisticsResponse.builder()
                        .mflCode(mflCode)
                        .from(effectiveFrom)
                        .to(effectiveTo)
                        .labOrdersSent(longValue(row.get("orders_sent", Long.class)))
                        .labOrdersAcknowledged(longValue(row.get("orders_acknowledged", Long.class)))
                        .labResultsReceived(longValue(row.get("results_received", Long.class)))
                        .labResultsValid(longValue(row.get("results_valid", Long.class)))
                        .labResultsUnsolicited(longValue(row.get("results_unsolicited", Long.class)))
                        .build())
                .one();

        // Enrich with the facility name when the MFL code resolves; thenReturn still
        // emits `stats` (with a null name) when it does not.
        return counts.flatMap(stats -> facilityName(mflCode)
                .doOnNext(stats::setFacilityName)
                .thenReturn(stats));
    }

    private Mono<String> facilityName(String mflCode) {
        return facilityService.cachedAll()
                .flatMap(facilities -> Mono.justOrEmpty(facilities.stream()
                        .filter(f -> mflCode.equals(f.getMflCode()))
                        .map(Facility::getName)
                        .findFirst()));
    }

    private static long longValue(Long value) {
        return value != null ? value : 0L;
    }
}
