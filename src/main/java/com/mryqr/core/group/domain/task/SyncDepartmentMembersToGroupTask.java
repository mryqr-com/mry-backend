package com.mryqr.core.group.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.mryqr.common.domain.user.User.NO_USER;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncDepartmentMembersToGroupTask implements RetryableTask {
    private final DepartmentRepository departmentRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    public void run(String departmentId) {
        List<Group> groups = groupRepository.byDepartmentId(departmentId);
        if (isEmpty(groups)) {
            return;
        }

        departmentRepository.byIdOptional(departmentId).ifPresent(department -> {
            Set<String> members = memberRepository.cachedMemberIdsOfDepartment(department.getTenantId(), departmentId);
            groups.forEach(group -> {
                group.syncDepartment(department, members, NO_USER);
                groupRepository.save(group);
                log.info("Synced department[{}] with members to group[{}].", departmentId, group.getId());
            });
        });


    }

}
