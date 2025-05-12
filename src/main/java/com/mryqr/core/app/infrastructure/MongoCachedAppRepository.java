package com.mryqr.core.app.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.TenantCachedApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MryConstants.APP_CACHE;
import static com.mryqr.core.common.utils.MryConstants.APP_COLLECTION;
import static com.mryqr.core.common.utils.MryConstants.TENANT_APPS_CACHE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

//为了绕开Spring AOP必须从外部调用才生效的限制，否则方法可以直接放到AppRepository中
//不要直接使用，而是使用AppRepository中同名方法
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedAppRepository extends MongoBaseRepository<App> {

    @Cacheable(value = APP_CACHE, key = "#appId")
    public App cachedById(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        return super.byId(appId);
    }

    //必须返回ArrayList而非List，否则缓存中由于没有ArrayList类型信息而失败
    @Cacheable(value = TENANT_APPS_CACHE, key = "#tenantId")
    public ArrayList<TenantCachedApp> cachedTenantAllApps(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId)).with(by(ASC, "createdAt"));
        query.fields().include("name", "icon", "managers", "permission", "operationPermission", "active", "groupSynced", "locked");

        return new ArrayList<>(mongoTemplate.find(query, TenantCachedApp.class, APP_COLLECTION));
    }

    @Caching(evict = {@CacheEvict(value = APP_CACHE, key = "#appId")})
    public void evictAppCache(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        log.info("Evicted cache for app[{}].", appId);
    }

    @Caching(evict = {@CacheEvict(value = TENANT_APPS_CACHE, key = "#tenantId")})
    public void evictTenantAppsCache(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        log.info("Evicted all apps cache for tenant[{}].", tenantId);
    }

}
