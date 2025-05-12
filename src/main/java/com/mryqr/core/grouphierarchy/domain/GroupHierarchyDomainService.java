package com.mryqr.core.grouphierarchy.domain;

import com.mryqr.core.common.domain.idnode.IdTree;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_HIERARCHY_NOT_MATCH;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_NAME_DUPLICATES;
import static java.util.Set.copyOf;

@Component
@RequiredArgsConstructor
public class GroupHierarchyDomainService {
    private final GroupRepository groupRepository;

    public void updateGroupHierarchy(GroupHierarchy groupHierarchy, IdTree idTree, User user) {
        groupHierarchy.update(idTree, user);

        Set<String> providedAllGroupIds = groupHierarchy.allGroupIds();
        List<AppCachedGroup> appCachedGroups = groupRepository.cachedAppAllGroups(groupHierarchy.getAppId());

        Set<String> allGroupIds = appCachedGroups.stream()
                .map(AppCachedGroup::getId)
                .collect(toImmutableSet());
        if (!Objects.equals(allGroupIds, providedAllGroupIds)) {
            throw new MryException(GROUP_HIERARCHY_NOT_MATCH, "分组层级与应用分组不匹配。");
        }

        Map<String, String> allGroupNames = appCachedGroups.stream()
                .collect(toImmutableMap(AppCachedGroup::getId, AppCachedGroup::getName));
        Map<String, String> allFullNames = groupHierarchy.groupFullNames(allGroupNames);
        if (allFullNames.size() > copyOf(allFullNames.values()).size()) {
            throw new MryException(GROUP_NAME_DUPLICATES, "更新失败，存在名称重复。", "appId", groupHierarchy.getAppId());
        }
    }
}
