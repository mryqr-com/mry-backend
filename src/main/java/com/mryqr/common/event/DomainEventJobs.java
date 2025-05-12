package com.mryqr.common.event;

import com.mongodb.client.result.DeleteResult;
import com.mryqr.common.properties.MryRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import static com.mryqr.common.utils.MryConstants.CONSUMING_DOMAIN_EVENT_COLLECTION;
import static com.mryqr.common.utils.MryConstants.PUBLISHING_DOMAIN_EVENT_COLLECTION;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventJobs {
    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final MryRedisProperties mryRedisProperties;

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void removeOldPublishingDomainEventsFromMongo(int days) {
        log.info("Start remove old publishing domain events from mongodb.");
        Query query = Query.query(where("raisedAt").lt(now().minus(days, DAYS)));
        DeleteResult result = mongoTemplate.remove(query, PUBLISHING_DOMAIN_EVENT_COLLECTION);
        log.info("Removed {} old publishing domain events from mongodb which are more than 100 days old.", result.getDeletedCount());
    }

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void removeOldConsumingDomainEventsFromMongo(int days) {
        log.info("Start remove old consuming domain events from mongodb.");
        Query query = Query.query(where("consumedAt").lt(now().minus(days, DAYS)));
        DeleteResult result = mongoTemplate.remove(query, CONSUMING_DOMAIN_EVENT_COLLECTION);
        log.info("Removed {} old consuming domain events from mongodb which are more than 100 days old.", result.getDeletedCount());
    }

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void removeOldDomainEventsFromRedis(int count, boolean approximate) {
        log.info("Start remove old domain events from redis stream.");
        mryRedisProperties.allDomainEventStreams().forEach(stream -> {
            Long domainEventCount = stringRedisTemplate.opsForStream().trim(stream, count, approximate);
            if (domainEventCount != null) {
                log.info("Removed {} old domains events from redis stream[{}].", domainEventCount, stream);
            }
        });
    }

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void removeOldWebhookEventsFromRedis(int count, boolean approximate) {
        log.info("Start remove old webhook events from redis stream.");
        Long webhookEventCount = stringRedisTemplate.opsForStream().trim(mryRedisProperties.getWebhookStream(), count, approximate);
        if (webhookEventCount != null) {
            log.info("Removed {} old webhook events from redis stream.", webhookEventCount);
        }
    }

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void removeOldNotificationEventsFromRedis(int count, boolean approximate) {
        log.info("Start remove old notification events from redis stream.");
        Long notificationEventCount = stringRedisTemplate.opsForStream().trim(mryRedisProperties.getNotificationStream(), count, approximate);
        if (notificationEventCount != null) {
            log.info("Removed {} old notification events from redis stream.", notificationEventCount);
        }
    }

}
