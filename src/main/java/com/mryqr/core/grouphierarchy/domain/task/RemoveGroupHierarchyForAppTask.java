package com.mryqr.core.grouphierarchy.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveGroupHierarchyForAppTask implements RetryableTask {
    private final GroupHierarchyRepository groupHierarchyRepository;

    public void run(String appId) {
        int count = groupHierarchyRepository.removeGroupHierarchyUnderApp(appId);
        if (count > 0) {
            log.info("Removed group hierarchy for app[{}].", appId);
        }
    }
}
