package com.mryqr.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;

// Calls into CacheManager explicitly for evicting caches
// Used in situations where @CacheEvict is not suitable, eg. when the @CacheEvict method is called from within the same class

@Component
@RequiredArgsConstructor
public class CacheEvictor {
    private final CacheManager cacheManager;

    public void evict(String cacheName, String key) {
        requireNonBlank(cacheName, "cacheName must not be blank.");
        requireNonBlank(key, "key must not be blank.");

        Cache cache = this.cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    public void evictAll(String cacheName) {
        requireNonBlank(cacheName, "cacheName must not be blank.");

        Cache cache = this.cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
