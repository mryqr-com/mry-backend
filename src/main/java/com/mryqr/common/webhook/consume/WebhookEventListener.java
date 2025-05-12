package com.mryqr.common.webhook.consume;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventListener implements StreamListener<String, ObjectRecord<String, String>> {
    private final MryObjectMapper mryObjectMapper;
    private final WebhookEventConsumer webhookEventConsumer;

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        String jsonString = message.getValue();
        DomainEvent domainEvent = mryObjectMapper.readValue(jsonString, DomainEvent.class);
        try {
            webhookEventConsumer.consume(domainEvent);
        } catch (Throwable t) {
            log.error("Failed to listen webhook event[{}:{}].", domainEvent.getType(), domainEvent.getId(), t);
        }
    }
}
