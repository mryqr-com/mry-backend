package com.mryqr.common.ratelimit;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.properties.CommonProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.mryqr.common.exception.ErrorCode.TOO_MANY_REQUEST;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MapUtils.mapOf;

@Component
public class RedisRateLimiter implements MryRateLimiter {
    private static final String KEY_PREFIX = "RateLimit:";
    private static final long WINDOW_MS = 2_000L;
    private final StringRedisTemplate stringRedisTemplate;
    private final boolean enabled;
    private final RedisScript<Long> luaScript;
    private final static String LUA_SCRIPT_STRING = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window_ms = tonumber(ARGV[2])
            local member = ARGV[3]
            
            -- Use Redis server time to avoid clock differences between instances.
            local time = redis.call('TIME')
            local now = tonumber(time[1]) * 1000
                + math.floor(tonumber(time[2]) / 1000)
            
            local cutoff = now - window_ms
            
            -- Remove requests outside the rolling window.
            redis.call('ZREMRANGEBYSCORE', key, '-inf', cutoff)
            
            -- Count requests in the current rolling window.
            local count = redis.call('ZCARD', key)
            
            if count >= limit then
                return 0
            end
            
            -- Record the accepted request.
            redis.call('ZADD', key, now, member)
            
            -- Automatically clean up inactive keys.
            redis.call('PEXPIRE', key, window_ms * 2)
            
            return 1""";

    public RedisRateLimiter(StringRedisTemplate stringRedisTemplate, CommonProperties commonProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.enabled = commonProperties.isLimitRate();
        this.luaScript = new DefaultRedisScript<>(LUA_SCRIPT_STRING, Long.class);
    }

    @Override
    public void applyFor(String tenantId, String key, int tps) {
        requireNonBlank(tenantId, "tenantId must not be blank.");
        requireNonBlank(key, "key must not be blank.");
        doApply("Tenant:" + tenantId + ":" + key, tps);
    }

    @Override
    public void applyFor(String key, int tps) {
        requireNonBlank(key, "key must not be blank.");
        doApply("Global:" + key, tps);
    }

    private void doApply(String key, int tps) {
        if (tps <= 0) {
            throw new IllegalArgumentException("tps must be greater than zero");
        }

        if (!this.enabled) {
            return;
        }

        String finalKey = KEY_PREFIX + key;
        String member = UUID.randomUUID().toString();

        Long result = stringRedisTemplate.execute(
                luaScript,
                List.of(finalKey),
                Long.toString((long) tps * WINDOW_MS / 1_000),
                Long.toString(WINDOW_MS),
                member
        );

        if (result == null) {
            throw new IllegalStateException("Redis rate limiter returned a null result.");
        }

        if (result == 0L) {
            throw new MryException(TOO_MANY_REQUEST, "当前请求量过大。", mapOf("key", key, "tps", tps));
        }

        if (result != 1L) {
            throw new IllegalStateException("Unexpected rate limiter result: " + result);
        }
    }
}
