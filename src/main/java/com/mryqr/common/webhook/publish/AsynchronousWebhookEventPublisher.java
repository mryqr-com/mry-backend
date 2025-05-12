package com.mryqr.common.webhook.publish;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.profile.NonCiProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NonCiProfile
@RequiredArgsConstructor
public class AsynchronousWebhookEventPublisher implements MryWebhookEventPublisher {
    private final RedisWebhookEventSender redisWebhookEventSender;

    @Override
    public void publish(DomainEvent domainEvent) {
        redisWebhookEventSender.send(domainEvent);
    }
}
