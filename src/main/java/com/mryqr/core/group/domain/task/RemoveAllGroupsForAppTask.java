package com.mryqr.core.group.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllGroupsForAppTask implements RepeatableTask {
    private final GroupRepository groupRepository;

    public void run(String appId) {
        int count = groupRepository.removeAllGroupsUnderApp(appId);
        log.info("Removed all {} groups under app[{}].", count, appId);
    }
}
