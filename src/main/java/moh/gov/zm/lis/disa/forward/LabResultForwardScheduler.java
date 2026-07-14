package moh.gov.zm.lis.disa.forward;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.lab.repository.LabResultRepository;
import moh.gov.zm.lis.redis.lock.DistributedLockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Retries downstream forwarding for reconciled results whose initial send failed
 * (or was left PENDING). Runs under a distributed lock so only one instance forwards
 * at a time; gives up after {@code MAX_ATTEMPTS}, leaving the row FAILED for manual review.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LabResultForwardScheduler {
    private static final short MAX_ATTEMPTS = 10;
    private static final int BATCH_SIZE = 50;

    private final LabResultRepository labResultRepository;
    private final LabResultForwarder forwarder;
    private final DistributedLockService lockService;

    @Scheduled(fixedDelayString = "30000")
    public void retryForwards() {
        lockService.withLock(DistributedLockService.LAB_RESULT_FORWARD_LOCK, Duration.ofSeconds(60),
                        labResultRepository.findForwardable(MAX_ATTEMPTS, BATCH_SIZE)
                                .concatMap(forwarder::forward)
                                .then())
                .block();
    }
}
