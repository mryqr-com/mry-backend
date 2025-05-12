package com.mryqr.core.inappnotification.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InAppNotificationFactory {
    public InAppNotification createInAppNotification(String memberId,
                                                     String tenantId,
                                                     String pcUrl,
                                                     String mobileUrl,
                                                     String content) {
        return new InAppNotification(memberId, tenantId, pcUrl, mobileUrl, content);
    }
}
