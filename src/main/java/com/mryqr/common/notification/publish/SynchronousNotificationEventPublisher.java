package com.mryqr.common.notification.publish;

import com.mryqr.common.notification.consume.NotificationEventConsumer;
import com.mryqr.core.common.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("ci")
@RequiredArgsConstructor
public class SynchronousNotificationEventPublisher implements NotificationEventPublisher {
    private final NotificationEventConsumer notificationEventConsumer;

    @Override
    public void publish(DomainEvent domainEvent) {
        notificationEventConsumer.consume(domainEvent);
    }
}
