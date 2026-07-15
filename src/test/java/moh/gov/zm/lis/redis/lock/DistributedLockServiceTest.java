package moh.gov.zm.lis.redis.lock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributedLockServiceTest {

    private static final String KEY = "lis:lock:test";
    private static final Duration TTL = Duration.ofSeconds(30);

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;
    @Mock
    private ReactiveValueOperations<String, String> valueOps;
    @Mock
    @SuppressWarnings("unchecked")
    private RedisScript<Long> unlockScript;

    private DistributedLockService service() {
        return new DistributedLockService(redisTemplate, unlockScript);
    }

    @Test
    void acquireLockTrueWhenSetIfAbsentSucceeds() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(KEY, "tok", TTL)).thenReturn(Mono.just(true));

        StepVerifier.create(service().acquireLock(KEY, "tok", TTL)).expectNext(true).verifyComplete();
    }

    @Test
    void acquireLockFalseWhenEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(KEY, "tok", TTL)).thenReturn(Mono.empty());

        StepVerifier.create(service().acquireLock(KEY, "tok", TTL)).expectNext(false).verifyComplete();
    }

    @Test
    void releaseLockTrueWhenScriptReturnsOne() {
        when(redisTemplate.execute(eq(unlockScript), eq(List.of(KEY)), eq(List.of("tok"))))
                .thenReturn(Flux.just(1L));

        StepVerifier.create(service().releaseLock(KEY, "tok")).expectNext(true).verifyComplete();
    }

    @Test
    void releaseLockFalseWhenScriptReturnsZero() {
        when(redisTemplate.execute(eq(unlockScript), eq(List.of(KEY)), eq(List.of("tok"))))
                .thenReturn(Flux.just(0L));

        StepVerifier.create(service().releaseLock(KEY, "tok")).expectNext(false).verifyComplete();
    }

    @Test
    void withLockRunsWorkAndReleasesWhenAcquired() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(eq(KEY), anyString(), eq(TTL))).thenReturn(Mono.just(true));
        when(redisTemplate.execute(eq(unlockScript), any(List.class), any(List.class))).thenReturn(Flux.just(1L));
        AtomicBoolean ran = new AtomicBoolean(false);

        StepVerifier.create(service().withLock(KEY, TTL, Mono.fromCallable(() -> {
                    ran.set(true);
                    return "done";
                })))
                .expectNext("done")
                .verifyComplete();

        org.assertj.core.api.Assertions.assertThat(ran).isTrue();
        // Lock is released via doFinally as a fire-and-forget subscribe on boundedElastic,
        // so await it with a timeout rather than verifying synchronously.
        verify(redisTemplate, timeout(2000)).execute(eq(unlockScript), eq(List.of(KEY)), any(List.class));
    }

    @Test
    void withLockSkipsWorkWhenNotAcquired() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(eq(KEY), anyString(), eq(TTL))).thenReturn(Mono.just(false));
        // release must never be attempted when the lock was not held
        lenient().when(redisTemplate.execute(any(RedisScript.class), any(List.class), any(List.class)))
                .thenReturn(Flux.just(1L));
        AtomicBoolean ran = new AtomicBoolean(false);

        StepVerifier.create(service().withLock(KEY, TTL, Mono.fromCallable(() -> {
                    ran.set(true);
                    return "done";
                })))
                .verifyComplete(); // empty — work skipped

        org.assertj.core.api.Assertions.assertThat(ran).isFalse();
        verify(redisTemplate, never()).execute(any(RedisScript.class), any(List.class), any(List.class));
    }
}
