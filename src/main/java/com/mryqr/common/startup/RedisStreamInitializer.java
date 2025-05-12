package com.mryqr.common.startup;

import com.mryqr.common.properties.MryRedisProperties;
import io.lettuce.core.RedisBusyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import static com.mryqr.common.utils.MryConstants.*;

@Slf4j
@Component("redisStreamInitializer")
public class RedisStreamInitializer {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MryRedisProperties mryRedisProperties;

    public RedisStreamInitializer(RedisTemplate<String, Object> redisTemplate, MryRedisProperties mryRedisProperties) {
        this.redisTemplate = redisTemplate;
        this.mryRedisProperties = mryRedisProperties;
        ensureConsumerGroupsExist();
    }

    private void ensureConsumerGroupsExist() {
        StreamOperations<String, Object, Object> operations = redisTemplate.opsForStream();
        mryRedisProperties.allDomainEventStreams().forEach(stream -> tryCreateConsumerGroup(operations, stream, REDIS_DOMAIN_EVENT_CONSUMER_GROUP));
        tryCreateConsumerGroup(operations, mryRedisProperties.getWebhookStream(), REDIS_WEBHOOK_CONSUMER_GROUP);
        tryCreateConsumerGroup(operations, mryRedisProperties.getNotificationStream(), REDIS_NOTIFICATION_CONSUMER_GROUP);
    }

    private void tryCreateConsumerGroup(StreamOperations<String, Object, Object> operations, String streamKey, String group) {
        try {
            operations.createGroup(streamKey, group);
            log.info("Created redis consumer group[{}] for stream[{}].", group, streamKey);
        } catch (RedisSystemException ex) {
            var cause = ex.getRootCause();
            if (cause != null && RedisBusyException.class.equals(cause.getClass())) {
                log.warn("Redis group[{}] for stream[{}] already exists, skip create group.", group, streamKey);
            } else {
                throw ex;
            }
        }
    }

}
