package com.mryqr.common.webhook.publish;

import com.mryqr.common.event.DomainEvent;

public interface MryWebhookEventPublisher {
    void publish(DomainEvent domainEvent);
}
