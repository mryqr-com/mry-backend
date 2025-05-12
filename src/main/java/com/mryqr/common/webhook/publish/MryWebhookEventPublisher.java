package com.mryqr.common.webhook.publish;

import com.mryqr.core.common.domain.event.DomainEvent;

public interface MryWebhookEventPublisher {
    void publish(DomainEvent domainEvent);
}
