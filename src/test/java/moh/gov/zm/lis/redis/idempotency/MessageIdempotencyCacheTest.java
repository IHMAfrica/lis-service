package moh.gov.zm.lis.redis.idempotency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageIdempotencyCacheTest {

    private static final String ID = "MC-123";
    private static final String KEY = "lis:idempotency:msg:MC-123";

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;
    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    private MessageIdempotencyCache cache() {
        return new MessageIdempotencyCache(redisTemplate);
    }

    @Test
    void isProcessedTrueWhenKeyPresent() {
        when(redisTemplate.hasKey(KEY)).thenReturn(Mono.just(true));

        StepVerifier.create(cache().isProcessed(ID)).expectNext(true).verifyComplete();
    }

    @Test
    void isProcessedFalseWhenKeyAbsent() {
        when(redisTemplate.hasKey(KEY)).thenReturn(Mono.just(false));

        StepVerifier.create(cache().isProcessed(ID)).expectNext(false).verifyComplete();
    }

    @Test
    void isProcessedFallsBackToFalseWhenRedisErrors() {
        when(redisTemplate.hasKey(KEY)).thenReturn(Mono.error(new RuntimeException("redis down")));

        // Must degrade gracefully so processing falls through to the DB check.
        StepVerifier.create(cache().isProcessed(ID)).expectNext(false).verifyComplete();
    }

    @Test
    void markProcessedSetsKeyWith24hTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.set(eq(KEY), eq("1"), eq(Duration.ofHours(24)))).thenReturn(Mono.just(true));

        StepVerifier.create(cache().markProcessed(ID)).verifyComplete();
        verify(valueOps).set(eq(KEY), eq("1"), eq(Duration.ofHours(24)));
    }

    @Test
    void markProcessedSwallowsRedisError() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.set(any(), any(), any(Duration.class)))
                .thenReturn(Mono.error(new RuntimeException("redis down")));

        // A caching failure must not break the processing pipeline.
        StepVerifier.create(cache().markProcessed(ID)).verifyComplete();
    }

    @Test
    void evictDeletesKey() {
        when(redisTemplate.delete(KEY)).thenReturn(Mono.just(1L));

        StepVerifier.create(cache().evict(ID)).verifyComplete();
        verify(redisTemplate).delete(KEY);
    }
}
