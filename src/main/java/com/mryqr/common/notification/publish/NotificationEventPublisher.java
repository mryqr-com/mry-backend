package com.mryqr.common.notification.publish;

import com.mryqr.common.event.DomainEvent;

public interface NotificationEventPublisher {
    void publish(DomainEvent domainEvent);
}
