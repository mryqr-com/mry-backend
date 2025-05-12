package com.mryqr.core.grouphierarchy.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.mryqr.common.exception.ErrorCode.GROUP_HIERARCHY_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoGroupHierarchyRepository extends MongoBaseRepository<GroupHierarchy> implements GroupHierarchyRepository {
    private final MongoTemplate mongoTemplate;
    private final MongoCachedGroupHierarchyRepository cachedGroupHierarchyRepository;

    @Override
    public GroupHierarchy byAppId(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("appId").is(appId));
        GroupHierarchy groupHierarchy = mongoTemplate.findOne(query, GroupHierarchy.class);

        if (groupHierarchy == null) {
            throw new MryException(GROUP_HIERARCHY_NOT_FOUND, "未找到分组层级。", mapOf("appId", appId));
        }

        return groupHierarchy;
    }

    @Override
    public GroupHierarchy byAppIdAndCheckTenantShip(String appId, User user) {
        requireNonBlank(appId, "App ID must not be blank.");

        GroupHierarchy groupHierarchy = byAppId(appId);
        checkTenantShip(groupHierarchy, user);
        return groupHierarchy;
    }

    @Override
    public GroupHierarchy cachedByAppId(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        return cachedGroupHierarchyRepository.cachedByAppId(appId);
    }

    @Override
    public int count(String tenantId) {
        return super.count(tenantId);
    }

    @Override
    public void save(GroupHierarchy groupHierarchy) {
        super.save(groupHierarchy);
        cachedGroupHierarchyRepository.evictGroupHierarchyCache(groupHierarchy.getAppId());
    }

    @Override
    public void evictGroupHierarchyCache(String appId) {
        cachedGroupHierarchyRepository.evictGroupHierarchyCache(appId);
    }

    @Override
    public int removeGroupHierarchyUnderApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, GroupHierarchy.class);
        return (int) result.getDeletedCount();
    }
}
