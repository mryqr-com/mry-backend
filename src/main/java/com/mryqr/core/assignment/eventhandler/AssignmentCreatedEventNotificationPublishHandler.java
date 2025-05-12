package com.mryqr.core.assignment.eventhandler;

import com.mryqr.common.notification.publish.NotificationEventPublisher;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.OneTimeDomainEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.ASSIGNMENT_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentCreatedEventNotificationPublishHandler extends OneTimeDomainEventHandler {
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == ASSIGNMENT_CREATED;
    }

    @Override
    protected void doHandle(DomainEvent domainEvent) {
        notificationEventPublisher.publish(domainEvent);
    }

}
