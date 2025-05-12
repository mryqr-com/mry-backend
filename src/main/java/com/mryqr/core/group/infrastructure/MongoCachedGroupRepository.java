package com.mryqr.core.group.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.AppCachedGroups;
import com.mryqr.core.group.domain.Group;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MryConstants.*;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
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

    @Cacheable(value = APP_GROUPS_CACHE, key = "#appId")
    public AppCachedGroups cachedAppAllGroups(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("appId").is(appId));
        query.fields().include("appId", "name", "managers", "members", "archived", "active", "customId", "departmentId");
        List<AppCachedGroup> appCachedGroups = mongoTemplate.find(query, AppCachedGroup.class, GROUP_COLLECTION);
        return AppCachedGroups.builder().groups(emptyIfNull(appCachedGroups)).build();
    }

    @Caching(evict = {@CacheEvict(value = GROUP_CACHE, key = "#groupId")})
    public void evictGroupCache(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        log.debug("Evicted cache for group[{}].", groupId);
    }

    @Caching(evict = {@CacheEvict(value = APP_GROUPS_CACHE, key = "#appId")})
    public void evictAppGroupsCache(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        log.debug("Evicted all groups cache for app[{}].", appId);
    }
}
