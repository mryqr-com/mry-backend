package com.mryqr.common.event.consume;

import com.mryqr.common.tracing.MryTracingService;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventConsumer;
import com.mryqr.core.common.utils.MryObjectMapper;
import io.micrometer.tracing.ScopedSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventListener implements StreamListener<String, ObjectRecord<String, String>> {
    private final MryObjectMapper mryObjectMapper;
    private final DomainEventConsumer domainEventConsumer;
    private final MryTracingService mryTracingService;

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        ScopedSpan scopedSpan = mryTracingService.startNewSpan("domain-event-listener");

        String jsonString = message.getValue();
        DomainEvent domainEvent = mryObjectMapper.readValue(jsonString, DomainEvent.class);
        try {
            domainEventConsumer.consume(domainEvent);
        } catch (Throwable t) {
            log.error("Failed to listen domain event[{}:{}].", domainEvent.getType(), domainEvent.getId(), t);
        }

        scopedSpan.end();
    }
}
