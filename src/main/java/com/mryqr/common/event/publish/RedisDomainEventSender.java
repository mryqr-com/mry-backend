package com.mryqr.common.event.publish;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventDao;
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
public class RedisDomainEventSender {
    private final MryObjectMapper mryObjectMapper;
    private final MryRedisProperties mryRedisProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final DomainEventDao domainEventDao;

    public void send(DomainEvent event) {
        try {
            String eventString = mryObjectMapper.writeValueAsString(event);
            ObjectRecord<String, String> record = StreamRecords.newRecord()
                    .ofObject(eventString)
                    .withStreamKey(mryRedisProperties.getDomainEventStream());
            stringRedisTemplate.opsForStream().add(record);
            domainEventDao.successPublish(event);
        } catch (Throwable t) {
            log.error("Error happened while publish domain event[{}:{}] to redis.", event.getType(), event.getId(), t);
            domainEventDao.failPublish(event);
        }
    }

}
