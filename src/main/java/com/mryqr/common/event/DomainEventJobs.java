package com.mryqr.common.event;

import com.mongodb.client.result.DeleteResult;
import com.mryqr.common.event.publish.RedisDomainEventSender;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventDao;
import com.mryqr.core.common.properties.MryRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.common.utils.MryConstants.EVENT_COLLECTION;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventJobs {
    private final DomainEventDao domainEventDao;
    private final RedisDomainEventSender redisDomainEventSender;
    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final MryRedisProperties mryRedisProperties;
    private final LockingTaskExecutor lockingTaskExecutor;

    public int publishDomainEvents() {
        try {
            //通过分布式锁保证只有一个publisher工作，以此保证消息发送的顺序
            TaskResult<Integer> result = lockingTaskExecutor.executeWithLock(this::doPublishDomainEvents,
                    new LockConfiguration(now(), "publish-domain-events", ofMinutes(1), ofMillis(1)));
            Integer publishedCount = result.getResult();
            return publishedCount != null ? publishedCount : 0;
        } catch (Throwable e) {
            log.error("Error while publish domain events.", e);
            return 0;
        }
    }

    private int doPublishDomainEvents() {
        int count = 0;
        int max = 10000;//每次运行最多发送的条数
        String startEventId = "EVT00000000000000001";//从最早的ID开始算起

        while (true) {
            List<DomainEvent> domainEvents = domainEventDao.tobePublishedEvents(startEventId, 100);
            if (isEmpty(domainEvents)) {
                break;
            }

            for (DomainEvent event : domainEvents) {
                redisDomainEventSender.send(event);
            }

            count = domainEvents.size() + count;
            if (count >= max) {
                break;
            }
            startEventId = domainEvents.get(domainEvents.size() - 1).getId();//下一次直接从最后一条开始查询
        }

        return count;
    }

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void removeOldDomainEventsFromMongo(int days) {
        log.info("Start remove old domain events from mongodb.");
        Query query = Query.query(where("raisedAt").lt(now().minus(days, DAYS)));
        DeleteResult result = mongoTemplate.remove(query, EVENT_COLLECTION);
        log.info("Removed {} old domain events from mongodb which are more than 100 days old.", result.getDeletedCount());
    }

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void removeOldDomainEventsFromRedis(int count, boolean approximate) {
        log.info("Start remove old domain events from redis stream.");
        Long domainEventCount = stringRedisTemplate.opsForStream().trim(mryRedisProperties.getDomainEventStream(), count, approximate);
        if (domainEventCount != null) {
            log.info("Removed {} old domains events from redis stream.", domainEventCount);
        }
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
