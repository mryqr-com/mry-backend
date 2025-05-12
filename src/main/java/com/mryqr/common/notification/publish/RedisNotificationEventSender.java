package com.mryqr.common.notification.publish;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.properties.MryRedisProperties;
import com.mryqr.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationEventSender {
    private final MryObjectMapper mryObjectMapper;
    private final MryRedisProperties mryRedisProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public void send(DomainEvent event) {
        String eventString = mryObjectMapper.writeValueAsString(event);
        ObjectRecord<String, String> record = StreamRecords.newRecord()
                .ofObject(eventString)
                .withStreamKey(mryRedisProperties.getNotificationStream());
        stringRedisTemplate.opsForStream().add(record);
    }

}
