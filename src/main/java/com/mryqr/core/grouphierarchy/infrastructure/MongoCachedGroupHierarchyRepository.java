package com.mryqr.core.grouphierarchy.infrastructure;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.mryqr.common.exception.ErrorCode.GROUP_HIERARCHY_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.GROUP_HIERARCHY_CACHE;
import static org.springframework.data.mongodb.core.query.Criteria.where;

//为了绕开Spring AOP必须从外部调用才生效的限制，否则方法可以直接放到GroupHierarchyRepository中
//不要直接使用，而是使用GroupHierarchyRepository中同名方法
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedGroupHierarchyRepository extends MongoBaseRepository<GroupHierarchy> {

    @Cacheable(value = GROUP_HIERARCHY_CACHE, key = "#appId")
    public GroupHierarchy cachedByAppId(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("appId").is(appId));
        GroupHierarchy groupHierarchy = mongoTemplate.findOne(query, GroupHierarchy.class);

        if (groupHierarchy == null) {
            throw new MryException(GROUP_HIERARCHY_NOT_FOUND, "未找到分组层级。", mapOf("appId", appId));
        }

        return groupHierarchy;
    }

    @Caching(evict = {@CacheEvict(value = GROUP_HIERARCHY_CACHE, key = "#appId")})
    public void evictGroupHierarchyCache(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        log.debug("Evicted group hierarchy cache for app[{}].", appId);
    }

}
