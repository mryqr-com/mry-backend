package com.mryqr.common.cache;

import com.mryqr.common.utils.MryObjectMapper;
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
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.mryqr.common.utils.MryConstants.*;
import static java.time.Duration.ofDays;
import static java.time.Duration.ofHours;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration {
    private static final String CACHE_PREFIX = "Cache:";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisBuilderCustomizer(MryObjectMapper objectMapper) {
        MryObjectMapper defaultObjectMapper = new MryObjectMapper();
        defaultObjectMapper.activateDefaultTyping(defaultObjectMapper.getPolymorphicTypeValidator(), NON_FINAL, PROPERTY);
        GenericJackson2JsonRedisSerializer defaultSerializer = new GenericJackson2JsonRedisSerializer(defaultObjectMapper);
        var tenantCachedMembersSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, TenantCachedMembers.class);
        var tenantCachedAppsSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, TenantCachedApps.class);
        var tenantCachedDepartmentsSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, TenantCachedDepartments.class);
        var appCachedGroupsSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, AppCachedGroups.class);
        var openAssignmentPagesSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, OpenAssignmentPages.class);
        var appSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, App.class);
        var groupSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Group.class);
        var groupHierarchySerializer = new Jackson2JsonRedisSerializer<>(objectMapper, GroupHierarchy.class);
        var deptHierarchySerializer = new Jackson2JsonRedisSerializer<>(objectMapper, DepartmentHierarchy.class);
        var memberSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Member.class);
        var tenantSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Tenant.class);

        return builder -> builder.cacheDefaults(defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(defaultSerializer))
                        .entryTtl(ofDays(1)))
                .withCacheConfiguration(TENANT_MEMBERS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(tenantCachedMembersSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(TENANT_APPS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(tenantCachedAppsSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(TENANT_DEPARTMENTS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(tenantCachedDepartmentsSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(APP_GROUPS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(appCachedGroupsSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(OPEN_ASSIGNMENT_PAGES_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(openAssignmentPagesSerializer))
                        .entryTtl(ofHours(12)))
                .withCacheConfiguration(APP_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(appSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(GROUP_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(groupSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(GROUP_HIERARCHY_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(groupHierarchySerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(DEPARTMENT_HIERARCHY_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(deptHierarchySerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(MEMBER_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(memberSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(TENANT_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(tenantSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(API_TENANT_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(tenantSerializer))
                        .entryTtl(ofDays(7)));
    }
}
