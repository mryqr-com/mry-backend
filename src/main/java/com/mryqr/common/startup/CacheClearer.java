package com.mryqr.common.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import static com.mryqr.common.utils.MryConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClearer {

    @Caching(evict = {
            @CacheEvict(value = APP_CACHE, allEntries = true),
            @CacheEvict(value = TENANT_APPS_CACHE, allEntries = true),
            @CacheEvict(value = GROUP_CACHE, allEntries = true),
            @CacheEvict(value = APP_GROUPS_CACHE, allEntries = true),
            @CacheEvict(value = GROUP_HIERARCHY_CACHE, allEntries = true),
            @CacheEvict(value = MEMBER_CACHE, allEntries = true),
            @CacheEvict(value = TENANT_MEMBERS_CACHE, allEntries = true),
            @CacheEvict(value = TENANT_DEPARTMENTS_CACHE, allEntries = true),
            @CacheEvict(value = DEPARTMENT_HIERARCHY_CACHE, allEntries = true),
            @CacheEvict(value = TENANT_CACHE, allEntries = true),
            @CacheEvict(value = API_TENANT_CACHE, allEntries = true),
            @CacheEvict(value = OPEN_ASSIGNMENT_PAGES_CACHE, allEntries = true)
    })
    public void evictAllCache() {
        log.info("Evicted all cache.");
    }
}
