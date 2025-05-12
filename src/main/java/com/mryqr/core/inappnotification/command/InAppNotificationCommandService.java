package com.mryqr.core.inappnotification.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.inappnotification.domain.InAppNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationCommandService {
    private final InAppNotificationRepository inAppNotificationRepository;
    private final MryRateLimiter mryRateLimiter;

    public void viewInAppNotification(String inAppNotificationId, User user) {
        this.mryRateLimiter.applyFor(user.getTenantId(), "INA:View", 20);

        this.inAppNotificationRepository.markAsViewed(inAppNotificationId, user.getMemberId());
    }

    public void markAllNotificationsAsViewed(User user) {
        this.mryRateLimiter.applyFor(user.getTenantId(), "INA:ViewAll", 20);
        this.inAppNotificationRepository.markAllAsViewed(user.getMemberId());
    }
}
