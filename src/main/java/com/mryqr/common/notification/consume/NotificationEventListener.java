package com.mryqr.common.notification.consume;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener implements StreamListener<String, ObjectRecord<String, String>> {
    private final MryObjectMapper mryObjectMapper;
    private final NotificationEventConsumer notificationEventConsumer;

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        String jsonString = message.getValue();
        DomainEvent domainEvent = mryObjectMapper.readValue(jsonString, DomainEvent.class);
        try {
            notificationEventConsumer.consume(domainEvent);
        } catch (Throwable t) {
            log.error("Failed to listen notification event[{}:{}].", domainEvent.getType(), domainEvent.getId(), t);
        }
    }
}
