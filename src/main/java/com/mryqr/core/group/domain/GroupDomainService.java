package com.mryqr.core.group.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_WITH_CUSTOM_ID_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class GroupDomainService {
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;

    public void rename(Group group, String newName, User user) {
        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(group.getAppId());

        Set<String> siblingGroupIds = groupHierarchy.siblingGroupIdsOf(group.getId());
        if (isNotEmpty(siblingGroupIds)) {
            Set<String> siblingGroupNames = groupRepository.cachedAppAllGroups(group.getAppId()).stream()
                    .filter(cachedGroup -> siblingGroupIds.contains(cachedGroup.getId()))
                    .map(AppCachedGroup::getName)
                    .collect(toImmutableSet());

            if (siblingGroupNames.contains(newName)) {
                throw new MryException(GROUP_WITH_NAME_ALREADY_EXISTS, "重命名失败，名称已被占用。",
                        mapOf("groupId", group.getId(), "name", newName));
            }
        }

        group.rename(newName, user);
    }

    public void checkDeleteGroups(App app, Set<String> tobeDeletedGroupIds) {
        checkAtLeastOneVisibleGroupExists(app, tobeDeletedGroupIds, "删除");
    }

    public void checkDeactivateGroups(App app, Set<String> tobeDeactivatedGroupIds) {
        checkAtLeastOneVisibleGroupExists(app, tobeDeactivatedGroupIds, "禁用");
    }

    public void checkArchiveGroups(App app, Set<String> tobeArchivedGroupIds) {
        checkAtLeastOneVisibleGroupExists(app, tobeArchivedGroupIds, "归档");
    }

    private void checkAtLeastOneVisibleGroupExists(App app, Set<String> excludedGroupIds, String ops) {
        Set<String> remainActiveGroupIds = groupRepository.cachedAppAllGroups(app.getId()).stream()
                .filter(it -> !excludedGroupIds.contains(it.getId()) && it.isVisible() && !it.isArchived())
                .map(AppCachedGroup::getId)
                .collect(toImmutableSet());

        if (isEmpty(remainActiveGroupIds)) {
            String groupText = Objects.equals(app.groupDesignation(), "分组") ? "分组" : app.groupDesignation() + "或分组";
            throw new MryException(NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT,
                    ops + "失败，必须保留至少一个可见（非禁用且非归档）的" + groupText + "。",
                    mapOf("appId", app.getId()));
        }
    }

    public void updateCustomId(Group group, String customId, User user) {
        checkCustomIdDuplication(group, customId);
        group.updateCustomId(customId, user);
    }

    private void checkCustomIdDuplication(Group group, String customId) {
        if (isNotBlank(customId)
                && !Objects.equals(group.getCustomId(), customId)
                && groupRepository.cachedExistsByCustomId(customId, group.getAppId())) {
            throw new MryException(GROUP_WITH_CUSTOM_ID_ALREADY_EXISTS,
                    "自定义编号已被占用。",
                    mapOf("qrId", group.getId(), "customId", customId));
        }
    }
}
