package com.mryqr.common.event.publish.infrastructure;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.event.publish.DomainEventSender;
import com.mryqr.common.properties.MryRedisProperties;
import com.mryqr.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDomainEventSender implements DomainEventSender {
    private final MryObjectMapper mryObjectMapper;
    private final MryRedisProperties mryRedisProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public CompletableFuture<String> send(DomainEvent event) {
        try {
            String eventString = mryObjectMapper.writeValueAsString(event);
            ObjectRecord<String, String> record = StreamRecords.newRecord()
                    .ofObject(eventString)
                    .withStreamKey(mryRedisProperties.domainEventStreamForTenant(event.getArTenantId()));
            stringRedisTemplate.opsForStream().add(record);
            return CompletableFuture.completedFuture(event.getId());
        } catch (Throwable t) {
            log.error("Error happened while publish domain event[{}:{}] to redis.", event.getType(), event.getId(), t);
            return CompletableFuture.failedFuture(t);
        }
    }

}
