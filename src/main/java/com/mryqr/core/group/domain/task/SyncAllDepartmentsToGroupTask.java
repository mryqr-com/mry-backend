package com.mryqr.core.group.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupFactory;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.member.domain.TenantCachedMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.domain.user.User.NOUSER;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncAllDepartmentsToGroupTask implements RetryableTask {
    private final AppRepository appRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final DepartmentRepository departmentRepository;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final MemberRepository memberRepository;
    private final GroupFactory groupFactory;

    public void run(String appId) {
        appRepository.byIdOptional(appId).ifPresent(app -> {
            if (app.isGroupSynced()) {
                doSync(app.getId(), app.getTenantId());
                log.info("Synced all departments to groups for app[{}].", appId);
            }
        });
    }

    private void doSync(String appId, String tenantId) {
        //先清空缓存以保证从数据库加载最新的数据
        groupRepository.evictAppGroupsCache(appId);
        memberRepository.evictTenantMembersCache(tenantId);

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(tenantId);
        Set<String> allDepartmentIds = departmentHierarchy.allDepartmentIds();

        cleanNonExistDepartmentForAllGroups(allDepartmentIds, appId);
        if (isEmpty(allDepartmentIds)) {
            return;
        }

        Map<String, String> departmentToGroupMap = new HashMap<>();
        List<TenantCachedMember> allMembers = memberRepository.cachedTenantAllMembers(tenantId);
        allDepartmentIds.forEach(departmentId -> {
            Optional<Group> groupOptional = groupRepository.byDepartmentIdOptional(departmentId, appId);
            if (groupOptional.isPresent()) {//已有对应group则更新
                Group group = groupOptional.get();
                Department department = departmentRepository.byId(departmentId);
                List<String> members = allMemberIdsOfDepartment(departmentId, allMembers);
                group.syncDepartment(department, members, NOUSER);
                groupRepository.save(group);
                departmentToGroupMap.put(departmentId, group.getId());
            } else {//不存在对应group则新建
                Department department = departmentRepository.byId(departmentId);
                List<String> members = allMemberIdsOfDepartment(departmentId, allMembers);
                Group group = groupFactory.syncFrom(department, appId, NOUSER);
                group.syncDepartment(department, members, NOUSER);
                groupRepository.save(group);
                departmentToGroupMap.put(departmentId, group.getId());
            }
        });

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(appId);
        departmentToGroupMap.values().forEach(groupId -> groupHierarchy.removeGroup(groupId, NOUSER));
        groupHierarchy.merge(departmentHierarchy.getIdTree().map(departmentToGroupMap), NOUSER);
        groupHierarchyRepository.save(groupHierarchy);
    }

    private void cleanNonExistDepartmentForAllGroups(Set<String> allDepartmentIds, String appId) {
        List<AppCachedGroup> allExistingGroups = groupRepository.cachedAppAllGroups(appId);

        allExistingGroups.forEach(group -> {
            if (isNotBlank(group.getDepartmentId()) && !allDepartmentIds.contains(group.getDepartmentId())) {
                Group theGroup = groupRepository.byId(group.getId());
                theGroup.resetDepartment(NOUSER);
                groupRepository.save(theGroup);
            }
        });
    }

    private List<String> allMemberIdsOfDepartment(String departmentId, List<TenantCachedMember> allMembers) {
        return allMembers.stream()
                .filter(member -> member.getDepartmentIds().contains(departmentId))
                .map(TenantCachedMember::getId)
                .collect(toImmutableList());
    }
}
