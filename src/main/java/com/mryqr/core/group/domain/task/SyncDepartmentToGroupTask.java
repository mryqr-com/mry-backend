package com.mryqr.core.group.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.domain.user.User.NOUSER;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncDepartmentToGroupTask implements RetryableTask {
    private final DepartmentRepository departmentRepository;
    private final GroupRepository groupRepository;

    public void run(String departmentId) {
        List<Group> groups = groupRepository.byDepartmentId(departmentId);
        if (isEmpty(groups)) {
            return;
        }

        departmentRepository.byIdOptional(departmentId).ifPresent(department -> groups.forEach(group -> {
            group.syncDepartment(department, NOUSER);
            groupRepository.save(group);
            log.info("Synced department[{}] to group[{}] of app[{}].", departmentId, group.getId(), group.getAppId());
        }));
    }
}
