package com.mryqr.core.group.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_NOT_VISIBLE;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_SYNCED;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_WITH_CUSTOM_ID_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class GroupFactory {
    private final GroupRepository groupRepository;

    public Group create(String name, String parentGroupId, GroupHierarchy groupHierarchy, App app, User user) {
        checkNameDuplication(name, app.getId(), parentGroupId, groupHierarchy);
        checkGroupSynced(app);

        if (isNotBlank(parentGroupId)) {
            checkParentGroupVisible(parentGroupId);
        }
        return new Group(name, app, user);
    }

    public Group create(String name, String parentGroupId, GroupHierarchy groupHierarchy, App app, String customId, User user) {
        checkNameDuplication(name, app.getId(), parentGroupId, groupHierarchy);
        checkGroupSynced(app);

        if (isNotBlank(parentGroupId)) {
            checkParentGroupVisible(parentGroupId);
        }

        checkCustomIdDuplication(customId, app.getId());
        return new Group(name, app, customId, user);
    }

    public Group syncFrom(Department department, String appId, User user) {
        return new Group(department, appId, user);
    }

    private void checkNameDuplication(String name, String appId, String parentGroupId, GroupHierarchy groupHierarchy) {
        Set<String> siblingGroupIds = groupHierarchy.directChildGroupIdsUnder(parentGroupId);
        Set<String> siblingGroupNames = groupRepository.cachedAppAllGroups(appId).stream()
                .filter(cachedGroup -> siblingGroupIds.contains(cachedGroup.getId()))
                .map(AppCachedGroup::getName)
                .collect(toImmutableSet());

        if (siblingGroupNames.contains(name)) {
            throw new MryException(GROUP_WITH_NAME_ALREADY_EXISTS, "创建失败，名称已被占用。",
                    mapOf("name", name, "appId", appId));
        }
    }

    private void checkParentGroupVisible(String parentGroupId) {
        Group parentGroup = groupRepository.cachedById(parentGroupId);

        if (!parentGroup.isVisible()) {
            throw new MryException(GROUP_NOT_VISIBLE, "创建失败，上级分组不可见。", "parentGroupId", parentGroupId);
        }
    }

    private void checkCustomIdDuplication(String customId, String appId) {
        if (isNotBlank(customId) && groupRepository.cachedExistsByCustomId(customId, appId)) {
            throw new MryException(GROUP_WITH_CUSTOM_ID_ALREADY_EXISTS, "自定义编号已被占用。",
                    mapOf("customId", customId, "appId", appId));
        }
    }

    private void checkGroupSynced(App app) {
        if (app.isGroupSynced()) {
            throw new MryException(GROUP_SYNCED, "已设置部门同步，无法创建分组。", "appId", app.getId());
        }
    }
}
