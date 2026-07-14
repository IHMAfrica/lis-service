package moh.gov.zm.lis.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.ref.entity.InboundEventType;
import moh.gov.zm.lis.ref.entity.OutboundEventType;
import moh.gov.zm.lis.ref.repository.InboundEventTypeRepository;
import moh.gov.zm.lis.ref.repository.OutboundEventTypeRepository;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferenceDataCache {
    private static final String KEY_PREFIX = "lis:ref:";
    private static final Duration TTL = Duration.ofMinutes(600);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final OutboundEventTypeRepository outboundEventTypeRepository;
    private final InboundEventTypeRepository inboundEventTypeRepository;

    public Mono<Short> getInboundEventTypeId(String code) {
        return lookupId("inbound_event_type", code,
                inboundEventTypeRepository.findByCode(code).map(InboundEventType::getId));
    }

    public Mono<Short> getOutboundEventTypeId(String code) {
        return lookupId("outbound_event_type", code,
                outboundEventTypeRepository.findByCode(code).map(OutboundEventType::getId));
    }

    private Mono<Short> lookupId(String table, String code, Mono<Short> dbFallback) {
        String key = cacheKey(table, code);
        return redisTemplate.opsForValue().get(key)
                .map(Short::parseShort)
                .switchIfEmpty(dbFallback
                        .flatMap(id -> redisTemplate.opsForValue()
                                .set(key, id.toString(), TTL)
                                .thenReturn(id)))
                .onErrorResume(ex -> {
                    log.warn("Redis cache error for key='{}': {}", key, ex.getMessage());
                    return dbFallback;
                });
    }

    private static String cacheKey(String table, String code) {
        return KEY_PREFIX + table + ":code:" + code;
    }
}
