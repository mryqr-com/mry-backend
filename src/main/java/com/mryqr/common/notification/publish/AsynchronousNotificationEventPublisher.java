package com.mryqr.common.notification.publish;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.profile.NonCiProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NonCiProfile
@RequiredArgsConstructor
public class AsynchronousNotificationEventPublisher implements NotificationEventPublisher {
    private final RedisNotificationEventSender redisNotificationEventSender;

    @Override
    public void publish(DomainEvent domainEvent) {
        redisNotificationEventSender.send(domainEvent);
    }
}
