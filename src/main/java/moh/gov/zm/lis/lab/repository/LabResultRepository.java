package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.LabResult;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LabResultRepository extends R2dbcRepository<LabResult, UUID> {
    Mono<LabResult> findByMessageControlId(String messageControlId);

    /** All versions of a logical result, for supersession. */
    Flux<LabResult> findAllByResultKey(String resultKey);

    /**
     * Current results cleared to forward that still owe a downstream send:
     * auto-reconciled results, plus unsolicited results a clinician has accepted.
     */
    @Query("""
            SELECT * FROM lab.lab_result
            WHERE (reconciliation_status = 'RECONCILED' OR review_status = 'ACCEPTED')
              AND message_kind = 'RESULT'
              AND is_current = TRUE
              AND forward_status IN ('PENDING', 'FAILED')
              AND forward_attempts < :maxAttempts
            ORDER BY received_at
            LIMIT :batchSize
            """)
    Flux<LabResult> findForwardable(short maxAttempts, int batchSize);
}
