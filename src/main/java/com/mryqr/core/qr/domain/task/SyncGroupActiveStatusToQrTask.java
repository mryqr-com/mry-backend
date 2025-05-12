package com.mryqr.core.qr.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.common.domain.user.User.NO_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncGroupActiveStatusToQrTask implements RetryableTask {
    private final GroupRepository groupRepository;
    private final QrRepository qrRepository;

    public void run(String qrId) {
        qrRepository.byIdOptional(qrId).ifPresent(qr -> {
            Group group = groupRepository.byId(qr.getGroupId());
            if (qr.isGroupActive() != group.isActive()) {
                qr.updateGroupActiveStatus(group.isActive(), NO_USER);
                qrRepository.save(qr);
                log.info("Sync group[{}] active status to qr[{}].", group.getId(), qr.getId());
            }
        });
    }
}
