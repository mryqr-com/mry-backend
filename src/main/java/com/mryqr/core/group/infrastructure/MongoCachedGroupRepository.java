package com.mryqr.core.group.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.Group;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MryConstants.APP_GROUPS_CACHE;
import static com.mryqr.core.common.utils.MryConstants.GROUP_CACHE;
import static com.mryqr.core.common.utils.MryConstants.GROUP_COLLECTION;
import static org.springframework.data.mongodb.core.query.Criteria.where;

//为了绕开Spring AOP必须从外部调用才生效的限制，否则方法可以直接放到GroupRepository中
//不要直接使用，而是使用GroupRepository中同名方法
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedGroupRepository extends MongoBaseRepository<Group> {

    @Cacheable(value = GROUP_CACHE, key = "#groupId")
    public Group cachedById(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        return super.byId(groupId);
    }

    //必须返回ArrayList而非List，否则缓存中由于没有ArrayList类型信息而失败
    @Cacheable(value = APP_GROUPS_CACHE, key = "#appId")
    public ArrayList<AppCachedGroup> cachedAppAllGroups(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("appId").is(appId));
        query.fields().include("appId", "name", "managers", "members", "archived", "active", "customId", "departmentId");
        return new ArrayList<>(mongoTemplate.find(query, AppCachedGroup.class, GROUP_COLLECTION));
    }

    @Caching(evict = {@CacheEvict(value = GROUP_CACHE, key = "#groupId")})
    public void evictGroupCache(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        log.info("Evicted cache for group[{}].", groupId);
    }

    @Caching(evict = {@CacheEvict(value = APP_GROUPS_CACHE, key = "#appId")})
    public void evictAppGroupsCache(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        log.info("Evicted all groups cache for app[{}].", appId);
    }

}
