package com.mryqr.common.webhook.publish;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.properties.MryRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisWebhookEventSender {
    private final ObjectMapper objectMapper;
    private final MryRedisProperties mryRedisProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public void send(DomainEvent event) {
        String eventString = objectMapper.writeValueAsString(event);
        ObjectRecord<String, String> record = StreamRecords.newRecord()
                .ofObject(eventString)
                .withStreamKey(mryRedisProperties.getWebhookStream());
        stringRedisTemplate.opsForStream().add(record);
    }

}
