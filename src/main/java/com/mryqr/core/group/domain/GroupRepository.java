package com.mryqr.core.group.domain;

import com.mryqr.core.common.domain.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GroupRepository {
    Set<String> allGroupIdsOf(String appId);

    Group byCustomIdAndCheckTenantShip(String appId, String customId, User user);

    Optional<Group> byDepartmentIdOptional(String departmentId, String appId);

    List<Group> byDepartmentId(String departmentId);

    Group cachedById(String groupId);

    Group cachedByIdAndCheckTenantShip(String groupId, User user);

    Optional<Group> cachedByIdOptional(String groupId);

    List<AppCachedGroup> cachedAppAllGroups(String appId);

    Map<String, String> cachedAllGroupFullNames(String appId);

    Map<String, String> cachedGroupFullNamesOf(String appId, Collection<String> groupIds);

    Set<String> cachedAllVisibleGroupIds(String appId);

    Set<String> cachedWithAllSubVisibleGroupIds(String appId, String groupId);

    boolean cachedExistsByCustomId(String customId, String appId);

    boolean cachedAllGroupsExist(List<String> groupIds, String appId);

    Group byId(String id);

    Optional<Group> byIdOptional(String id);

    Group byIdAndCheckTenantShip(String id, User user);

    List<Group> byIds(Set<String> ids);

    int count(String tenantId);

    boolean exists(String groupId);

    void save(Group group);

    void save(List<Group> groups);

    void delete(Group group);

    void delete(List<Group> groups);

    void evictGroupCache(String groupId);

    void evictAppGroupsCache(String appId);

    int countGroupForApp(String appId);

    int removeAllGroupsUnderApp(String appId);

    int removeMemberFromAllGroups(String memberId, String tenantId);
}
