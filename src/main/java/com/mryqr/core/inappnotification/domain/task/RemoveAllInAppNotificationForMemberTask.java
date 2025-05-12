package com.mryqr.core.inappnotification.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.inappnotification.domain.InAppNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllInAppNotificationForMemberTask implements RetryableTask {
    private final InAppNotificationRepository inAppNotificationRepository;

    public void run(String memberId) {
        int count = inAppNotificationRepository.removeAllForMember(memberId);
        if (count > 0) {
            log.info("Removed all {} in app notifications for member[{}].", count, memberId);
        }
    }
}
