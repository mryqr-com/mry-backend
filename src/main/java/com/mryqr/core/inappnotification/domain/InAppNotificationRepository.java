package com.mryqr.core.inappnotification.domain;

import java.util.List;
import java.util.Optional;

public interface InAppNotificationRepository {
    void markAsViewed(String id, String memberId);

    void markAllAsViewed(String memberId);

    void insert(InAppNotification inAppNotification);

    void insert(List<InAppNotification> inAppNotifications);

    InAppNotification byId(String id);

    Optional<InAppNotification> byIdOptional(String id);

    int removeAllForMember(String memberId);
}
