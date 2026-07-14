package moh.gov.zm.lis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisConfig {
    /**
     * Lua script for safe distributed lock release.
     *
     * <p>Atomically checks that the caller still owns the lock (by comparing the stored
     * token to the expected value) before deleting the key. Returns {@code 1} if the
     * lock was released, {@code 0} if it was already expired or held by another caller.
     *
     * <pre>
     * KEYS[1] — lock key
     * ARGV[1] — lock token (UUID string) that the caller used when acquiring
     * </pre>
     */
    @Bean
    public RedisScript<Long> unlockScript() {
        String script = """
                if redis.call("get", KEYS[1]) == ARGV[1] then
                    return redis.call("del", KEYS[1])
                else
                    return 0
                end
                """;
        return RedisScript.of(script, Long.class);
    }
}
