package com.mryqr.core.tenant.infrastructure;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.tenant.domain.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.mryqr.common.exception.ErrorCode.TENANT_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.API_TENANT_CACHE;
import static com.mryqr.common.utils.MryConstants.TENANT_CACHE;
import static org.springframework.data.mongodb.core.query.Criteria.where;

//为了绕开Spring AOP必须从外部调用才生效的限制，否则方法可以直接放到TenantRepository中
//不要直接使用，而是使用TenantRepository中的同名方法
//由于Tenant的resourceUsage更新不会刷新缓存，因此不要在需要读取resourceUsage的时候使用该缓存
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedTenantRepository extends MongoBaseRepository<Tenant> {

    @Cacheable(value = TENANT_CACHE, key = "#tenantId")
    public Tenant cachedById(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return super.byId(tenantId);
    }

    @Cacheable(value = API_TENANT_CACHE, key = "#apiKey")
    public Tenant cachedByApiKey(String apiKey) {
        requireNonBlank(apiKey, "API Key must not be blank.");

        Query query = Query.query(where("apiSetting.apiKey").is(apiKey));
        Tenant tenant = mongoTemplate.findOne(query, Tenant.class);
        if (tenant == null) {
            throw new MryException(TENANT_NOT_FOUND, "没有找到ApiKey对应的租户。", mapOf("apiKey", apiKey));
        }

        return tenant;
    }

    @Caching(evict = {@CacheEvict(value = TENANT_CACHE, key = "#tenantId")})
    public void evictTenantCache(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        log.debug("Evicted tenant cache for tenant[{}].", tenantId);
    }

    @Caching(evict = {@CacheEvict(value = API_TENANT_CACHE, key = "#apiKey")})
    public void evictTenantCacheByApiKey(String apiKey) {
        requireNonBlank(apiKey, "API Key must not be blank.");

        log.debug("Evicted tenant cache for tenant[{}] by API key.", apiKey);
    }
}
