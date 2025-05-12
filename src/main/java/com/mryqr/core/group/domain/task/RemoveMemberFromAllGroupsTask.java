package com.mryqr.core.group.domain.task;

import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveMemberFromAllGroupsTask implements RepeatableTask {
    private final AppRepository appRepository;
    private final GroupRepository groupRepository;

    public void run(String memberId, String tenantId) {
        appRepository.allAppIdsOf(tenantId).forEach(appId -> {
            List<AppCachedGroup> groups = groupRepository.cachedAppAllGroups(appId);
            groups.stream().filter(group -> group.getMembers().contains(memberId))
                    .forEach(group -> groupRepository.evictGroupCache(group.getId()));

            if (groups.stream().anyMatch(group -> group.getMembers().contains(memberId))) {
                groupRepository.evictAppGroupsCache(appId);
            }
        });

        int count = groupRepository.removeMemberFromAllGroups(memberId, tenantId);
        log.info("Removed member[{}] from {} groups of tenant[{}].", memberId, count, tenantId);
    }
}
