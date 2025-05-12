package com.mryqr.core.assignment.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.notification.publish.NotificationEventPublisher;
import com.mryqr.core.assignment.event.AssignmentNearExpiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentNearExpiredEventNotificationPublishHandler extends AbstractDomainEventHandler<AssignmentNearExpiredEvent> {
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public void handle(AssignmentNearExpiredEvent event) {
        notificationEventPublisher.publish(event);
    }

    @Override
    public int priority() {
        return 100;
    }
}
