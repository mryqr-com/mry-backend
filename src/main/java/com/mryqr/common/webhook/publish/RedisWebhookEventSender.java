package com.mryqr.common.webhook.publish;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.properties.MryRedisProperties;
import com.mryqr.core.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisWebhookEventSender {
    private final MryObjectMapper mryObjectMapper;
    private final MryRedisProperties mryRedisProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public void send(DomainEvent event) {
        String eventString = mryObjectMapper.writeValueAsString(event);
        ObjectRecord<String, String> record = StreamRecords.newRecord()
                .ofObject(eventString)
                .withStreamKey(mryRedisProperties.getWebhookStream());
        stringRedisTemplate.opsForStream().add(record);
    }

}
