package com.mryqr.core.qr.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncGroupActiveStatusToQrsTask implements RepeatableTask {
    private final GroupRepository groupRepository;
    private final QrRepository qrRepository;

    public void run(String groupId) {
        groupRepository.byIdOptional(groupId).ifPresent(group -> {
            int count = qrRepository.syncGroupActiveStatusToQrs(group);
            log.info("Sync group active status[{}] to {} qrs under group[{}].", group.isActive(), count, groupId);
        });
    }
}
