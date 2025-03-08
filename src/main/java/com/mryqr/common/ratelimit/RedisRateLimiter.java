package com.mryqr.common.ratelimit;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.properties.CommonProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mryqr.common.exception.ErrorCode.TOO_MANY_REQUEST;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static java.lang.Integer.parseInt;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter implements MryRateLimiter {
    private final StringRedisTemplate stringRedisTemplate;
    private final CommonProperties commonProperties;

    @Override
    public void applyFor(String tenantId, String key, int tps) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");
        requireNonBlank(key, "Key must not be blank.");

        if (Objects.equals(tenantId, MRY_MANAGE_TENANT_ID)) {
            return;// do not apply for mry management tenant
        }

        //以5秒为周期统计
        doApply(key + ":" + tenantId + ":" + now().getEpochSecond() / 5,
                tps * 5,
                10,
                SECONDS);
    }

    @Override
    public void applyFor(String key, int tps) {
        requireNonBlank(key, "Key must not be blank.");

        //以5秒为周期统计
        doApply(key + ":" + now().getEpochSecond() / 5,
                tps * 5,
                10,
                SECONDS);
    }

    private void doApply(String key, int limit, int expire, TimeUnit expireUnit) {
        if (!commonProperties.isLimitRate()) {
            return;
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than 1.");
        }

        String finalKey = "RateLimit:" + key;
        String count = stringRedisTemplate.opsForValue().get(finalKey);
        if (isNotBlank(count) && parseInt(count) >= limit) {
            throw new MryException(TOO_MANY_REQUEST, "当前请求量过大。", mapOf("key", finalKey));
        }

        stringRedisTemplate.opsForValue().increment(finalKey);
        stringRedisTemplate.expire(finalKey, expire, expireUnit);
    }
}
