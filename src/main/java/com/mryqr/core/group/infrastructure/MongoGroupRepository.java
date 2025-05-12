package com.mryqr.core.group.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.infrastructure.MongoCachedGroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.AR_NOT_FOUND;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_NOT_FOUND;
import static com.mryqr.core.common.exception.ErrorCode.SYSTEM_ERROR;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.GROUP_COLLECTION;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoGroupRepository extends MongoBaseRepository<Group> implements GroupRepository {
    private final MongoCachedGroupRepository cachedGroupRepository;
    private final MongoCachedGroupHierarchyRepository cachedGroupHierarchyRepository;

    @Override
    public Set<String> allGroupIdsOf(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        return mongoTemplate.findDistinct(query, "_id", GROUP_COLLECTION, String.class).stream().collect(toImmutableSet());
    }

    @Override
    public Group byCustomIdAndCheckTenantShip(String appId, String customId, User user) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("appId").is(appId).and("customId").is(customId));
        Group group = mongoTemplate.findOne(query, Group.class);

        if (group == null) {
            throw new MryException(GROUP_NOT_FOUND, "未找到分组。", mapOf("appId", appId, "customId", customId));
        }

        checkTenantShip(group, user);
        return group;
    }

    @Override
    public Optional<Group> byDepartmentIdOptional(String departmentId, String appId) {
        requireNonBlank(departmentId, "Department ID must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("departmentId").is(departmentId).and("appId").is(appId));
        Group group = mongoTemplate.findOne(query, Group.class);
        return Optional.ofNullable(group);
    }

    @Override
    public List<Group> byDepartmentId(String departmentId) {
        requireNonBlank(departmentId, "Department ID must not be blank.");

        Query query = query(where("departmentId").is(departmentId));
        return mongoTemplate.find(query, Group.class);
    }

    @Override
    public Group cachedById(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        return cachedGroupRepository.cachedById(groupId);
    }

    @Override
    public Group cachedByIdAndCheckTenantShip(String groupId, User user) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        Group group = cachedGroupRepository.cachedById(groupId);
        checkTenantShip(group, user);
        return group;
    }

    @Override
    public Optional<Group> cachedByIdOptional(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        try {
            Group group = cachedGroupRepository.cachedById(groupId);
            return Optional.ofNullable(group);
        } catch (MryException ex) {
            if (ex.getCode() == AR_NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    @Override
    public List<AppCachedGroup> cachedAppAllGroups(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        return cachedGroupRepository.cachedAppAllGroups(appId);
    }

    @Override
    public Map<String, String> cachedAllGroupFullNames(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        GroupHierarchy groupHierarchy = cachedGroupHierarchyRepository.cachedByAppId(appId);
        Map<String, String> allGroupNames = cachedGroupRepository.cachedAppAllGroups(appId).stream()
                .collect(toImmutableMap(AppCachedGroup::getId, AppCachedGroup::getName));
        return groupHierarchy.groupFullNames(allGroupNames);
    }

    @Override
    public Map<String, String> cachedGroupFullNamesOf(String appId, Collection<String> groupIds) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonNull(groupIds, "Group IDs must not be blank.");

        if (isEmpty(groupIds)) {
            return Map.of();
        }

        GroupHierarchy groupHierarchy = cachedGroupHierarchyRepository.cachedByAppId(appId);
        Map<String, String> allGroupNames = cachedGroupRepository.cachedAppAllGroups(appId).stream()
                .collect(toImmutableMap(AppCachedGroup::getId, AppCachedGroup::getName));
        Map<String, String> groupFullNames = groupHierarchy.groupFullNames(allGroupNames);

        return groupFullNames.entrySet().stream()
                .filter(entry -> groupIds.contains(entry.getKey()))
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Set<String> cachedAllVisibleGroupIds(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        return cachedGroupRepository.cachedAppAllGroups(appId).stream()
                .filter(AppCachedGroup::isVisible)
                .map(AppCachedGroup::getId)
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> cachedWithAllSubVisibleGroupIds(String appId, String groupId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(groupId, "App ID must not be blank.");

        ArrayList<AppCachedGroup> allGroups = cachedGroupRepository.cachedAppAllGroups(appId);
        GroupHierarchy groupHierarchy = cachedGroupHierarchyRepository.cachedByAppId(appId);
        Set<String> subGroupIdsOf = groupHierarchy.withAllSubGroupIdsOf(groupId);

        return allGroups.stream().filter(group -> group.isVisible() && subGroupIdsOf.contains(group.getId()))
                .map(AppCachedGroup::getId)
                .collect(toImmutableSet());
    }

    @Override
    public boolean cachedExistsByCustomId(String customId, String appId) {
        requireNonBlank(customId, "Custom ID must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        return cachedGroupRepository.cachedAppAllGroups(appId).stream().anyMatch(app -> Objects.equals(app.getCustomId(), customId));
    }

    @Override
    public boolean cachedAllGroupsExist(List<String> groupIds, String appId) {
        requireNonNull(groupIds, "Group IDs must not be null");
        requireNonBlank(appId, "App ID must not be blank.");

        if (isEmpty(groupIds)) {
            return true;
        }

        return cachedGroupRepository.cachedAppAllGroups(appId).stream()
                .map(AppCachedGroup::getId)
                .collect(toImmutableSet())
                .containsAll(groupIds);
    }

    @Override
    public Group byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<Group> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public Group byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public List<Group> byIds(Set<String> ids) {
        return super.byIds(ids);
    }

    @Override
    public int count(String tenantId) {
        return super.count(tenantId);
    }

    @Override
    public boolean exists(String groupId) {
        return super.exists(groupId);
    }

    @Override
    public void save(Group group) {
        super.save(group);
        cachedGroupRepository.evictGroupCache(group.getId());
        cachedGroupRepository.evictAppGroupsCache(group.getAppId());
    }

    @Override
    public void save(List<Group> groups) {
        if (isEmpty(groups)) {
            return;
        }

        checkSameApp(groups);
        super.save(groups);
        groups.forEach(group -> cachedGroupRepository.evictGroupCache(group.getId()));
        groups.stream().findAny().ifPresent(group -> cachedGroupRepository.evictAppGroupsCache(group.getAppId()));
    }

    @Override
    public void delete(Group group) {
        super.delete(group);
        cachedGroupRepository.evictGroupCache(group.getId());
        cachedGroupRepository.evictAppGroupsCache(group.getAppId());
    }

    @Override
    public void delete(List<Group> groups) {
        if (isEmpty(groups)) {
            return;
        }

        checkSameApp(groups);
        super.delete(groups);
        groups.forEach(group -> cachedGroupRepository.evictGroupCache(group.getId()));
        groups.stream().findAny().ifPresent(group -> cachedGroupRepository.evictAppGroupsCache(group.getAppId()));
    }

    @Override
    public void evictGroupCache(String groupId) {
        cachedGroupRepository.evictGroupCache(groupId);
    }

    @Override
    public void evictAppGroupsCache(String appId) {
        cachedGroupRepository.evictAppGroupsCache(appId);
    }

    @Override
    public int countGroupForApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("appId").is(appId));
        long count = mongoTemplate.count(query, Group.class);
        return (int) count;
    }

    @Override
    public int removeAllGroupsUnderApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, Group.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeMemberFromAllGroups(String memberId, String tenantId) {
        requireNonBlank(memberId, "Member ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(new Criteria().orOperator(where("managers").is(memberId), where("members").is(memberId)));
        Update update = new Update().pull("managers", memberId).pull("members", memberId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, Group.class);
        return (int) result.getModifiedCount();
    }

    private void checkSameApp(List<Group> groups) {
        Set<String> appIds = groups.stream().map(Group::getAppId).collect(toImmutableSet());
        if (appIds.size() > 1) {
            Set<String> allGroupIds = groups.stream().map(AggregateRoot::getId).collect(toImmutableSet());
            throw new MryException(SYSTEM_ERROR, "All groups should belong to the same app.", "groupIds", allGroupIds);
        }
    }
}
