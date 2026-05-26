package com.mryqr.common.cache;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.TenantCachedApps;
import com.mryqr.core.assignment.domain.OpenAssignmentPages;
import com.mryqr.core.department.domain.TenantCachedDepartments;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.group.domain.AppCachedGroups;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.TenantCachedMembers;
import com.mryqr.core.tenant.domain.Tenant;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import static com.mryqr.common.utils.MryConstants.*;
import static java.time.Duration.ofDays;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration {
    private static final String CACHE_PREFIX = "Cache:";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisBuilderCustomizer(ObjectMapper objectMapper) {
        return builder -> builder
                .cacheDefaults(defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new GenericJacksonJsonRedisSerializer(objectMapper)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(TENANT_MEMBERS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, TenantCachedMembers.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(TENANT_APPS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, TenantCachedApps.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(TENANT_DEPARTMENTS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, TenantCachedDepartments.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(APP_GROUPS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, AppCachedGroups.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(OPEN_ASSIGNMENT_PAGES_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, OpenAssignmentPages.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(APP_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, App.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(GROUP_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, Group.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(GROUP_HIERARCHY_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, GroupHierarchy.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(DEPARTMENT_HIERARCHY_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, DepartmentHierarchy.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(MEMBER_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, Member.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(TENANT_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, Tenant.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(API_TENANT_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, Tenant.class)))
                        .entryTtl(ofDays(30)))
                ;
    }
}
