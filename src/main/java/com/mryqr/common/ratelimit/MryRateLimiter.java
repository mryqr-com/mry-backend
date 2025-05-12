package com.mryqr.common.ratelimit;


public interface MryRateLimiter {
    // 建议采用以下策略：
    // 最低访问量: 1TPS
    // 极低访问量: 5TPS
    // 较低访问量: 10TPS
    // 正常访问量: 20TPS
    // 较高访问量: 50TPS
    // 极高访问量: 100TPS
    void applyFor(String tenantId, String key, int tps);

    void applyFor(String key, int tps);
}
