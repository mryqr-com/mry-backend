package com.mryqr.common.webhook.publish;

import com.mryqr.common.webhook.consume.WebhookEventConsumer;
import com.mryqr.core.common.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("ci")
@RequiredArgsConstructor
public class SynchronousWebhookEventPublisher implements MryWebhookEventPublisher {
    private final WebhookEventConsumer webhookEventConsumer;

    @Override
    public void publish(DomainEvent domainEvent) {
        webhookEventConsumer.consume(domainEvent);
    }
}
