package com.mryqr.common.webhook.publish;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.profile.CiProfile;
import com.mryqr.common.webhook.consume.WebhookEventConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@CiProfile
@RequiredArgsConstructor
public class SynchronousWebhookEventPublisher implements MryWebhookEventPublisher {
    private final WebhookEventConsumer webhookEventConsumer;

    @Override
    public void publish(DomainEvent domainEvent) {
        webhookEventConsumer.consume(domainEvent);
    }
}
