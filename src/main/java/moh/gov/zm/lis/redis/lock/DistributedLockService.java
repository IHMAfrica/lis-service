package moh.gov.zm.lis.redis.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {
    public static final String OUTBOX_RELAY_LOCK = "lis:lock:outbox-relay";
    public static final String LAB_RESULT_FORWARD_LOCK = "lis:lock:lab-result-forward";
    public static final String MESSAGING_PURGE_LOCK = "lis:lock:messaging-purge";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RedisScript<Long> unlockScript;

    public Mono<Boolean> acquireLock(String key, String token, Duration ttl) {
        return redisTemplate.opsForValue()
                .setIfAbsent(key, token, ttl)
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> releaseLock(String key, String token) {
        return redisTemplate.execute(unlockScript, List.of(key), List.of(token))
                .next()
                .map(result -> result == 1L)
                .defaultIfEmpty(false);
    }

    public <T> Mono<T> withLock(String key, Duration ttl, Mono<T> work) {
        String token = UUID.randomUUID().toString();
        return acquireLock(key, token, ttl)
                .flatMap(acquired -> {
                    if (!acquired) {
                        log.debug("Lock not acquired for key='{}' — skipping cycle", key);
                        return Mono.empty();
                    }
                    log.debug("Lock acquired: key='{}', token='{}'", key, token);
                    return work
                            .publishOn(Schedulers.boundedElastic())
                            .doFinally(signal -> releaseLock(key, token)
                                    .doOnSuccess(released -> {
                                        if (Boolean.TRUE.equals(released)) {
                                            log.debug("Lock released: key='{}'", key);
                                        } else {
                                            log.warn("Lock already expired before release: key='{}'", key);
                                        }
                                    })
                                    .subscribe());
                });
    }
}
