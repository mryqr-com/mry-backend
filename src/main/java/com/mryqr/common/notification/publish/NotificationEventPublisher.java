package com.mryqr.common.notification.publish;

import com.mryqr.core.common.domain.event.DomainEvent;

public interface NotificationEventPublisher {
    void publish(DomainEvent domainEvent);
}
