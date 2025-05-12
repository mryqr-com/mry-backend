package com.mryqr.core.job;

import com.mryqr.BaseApiTest;
import com.mryqr.common.event.DomainEventJobs;
import com.mryqr.common.event.publish.RedisDomainEventSender;
import com.mryqr.common.notification.publish.RedisNotificationDomainEventSender;
import com.mryqr.common.webhook.publish.RedisWebhookEventSender;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.properties.MryRedisProperties;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.mryqr.core.common.domain.event.DomainEventStatus.CONSUME_FAILED;
import static com.mryqr.core.common.domain.event.DomainEventStatus.CREATED;
import static com.mryqr.core.common.domain.event.DomainEventStatus.PUBLISH_FAILED;
import static com.mryqr.core.common.domain.event.DomainEventStatus.PUBLISH_SUCCEED;
import static com.mryqr.core.common.domain.user.User.NOUSER;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class DomainEventJobsTest extends BaseApiTest {
    @Autowired
    private DomainEventJobs domainEventJobs;

    @Autowired
    private RedisDomainEventSender redisDomainEventSender;

    @Autowired
    private RedisWebhookEventSender redisWebhookEventSender;

    @Autowired
    private RedisNotificationDomainEventSender redisNotificationDomainEventSender;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MryRedisProperties mryRedisProperties;


    @Test
    public void should_publish_consume_failed_events() {
        QrCreatedEvent event = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);
        ReflectionTestUtils.setField(event, "raisedAt", now().minus(30, SECONDS));
        ReflectionTestUtils.setField(event, "status", CONSUME_FAILED);
        domainEventDao.insert(List.of(event));

        assertTrue(domainEventJobs.publishDomainEvents() >= 1);
        DomainEvent updatedEvent = domainEventDao.byId(event.getId());
        assertEquals(PUBLISH_SUCCEED, updatedEvent.getStatus());
    }

    @Test
    public void should_publish_publish_failed_events() {
        QrCreatedEvent event = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);
        ReflectionTestUtils.setField(event, "raisedAt", now().minus(30, SECONDS));
        ReflectionTestUtils.setField(event, "status", PUBLISH_FAILED);
        domainEventDao.insert(List.of(event));

        domainEventJobs.publishDomainEvents();
        DomainEvent updatedEvent = domainEventDao.byId(event.getId());
        assertEquals(PUBLISH_SUCCEED, updatedEvent.getStatus());
    }

    @Test
    public void should_not_publish_events_consumed_more_than_3_times() {
        QrCreatedEvent event = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);
        ReflectionTestUtils.setField(event, "raisedAt", now().minus(30, SECONDS));
        ReflectionTestUtils.setField(event, "consumedCount", 4);
        domainEventDao.insert(List.of(event));

        domainEventJobs.publishDomainEvents();
        DomainEvent updatedEvent = domainEventDao.byId(event.getId());
        assertEquals(CREATED, updatedEvent.getStatus());
    }

    @Test
    public void should_not_publish_events_published_more_than_3_times() {
        QrCreatedEvent event = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);
        ReflectionTestUtils.setField(event, "raisedAt", now().minus(30, SECONDS));
        ReflectionTestUtils.setField(event, "publishedCount", 4);
        domainEventDao.insert(List.of(event));

        domainEventJobs.publishDomainEvents();
        DomainEvent updatedEvent = domainEventDao.byId(event.getId());
        assertEquals(CREATED, updatedEvent.getStatus());
    }

    @Test
    public void should_remove_old_domain_events_from_mongo() {
        QrCreatedEvent event = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);
        ReflectionTestUtils.setField(event, "raisedAt", now().minus(300, DAYS));
        domainEventDao.insert(List.of(event));

        List<DomainEvent> dbEvents = domainEventDao.byIds(List.of(event.getId()));
        assertFalse(dbEvents.isEmpty());

        domainEventJobs.removeOldDomainEventsFromMongo(100);

        List<DomainEvent> updatedEvents = domainEventDao.byIds(List.of(event.getId()));
        assertTrue(updatedEvents.isEmpty());
    }

    @Test
    public void should_remove_old_domain_events_from_redis() {
        QrCreatedEvent event1 = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);
        QrCreatedEvent event2 = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);

        redisDomainEventSender.send(event1);
        redisDomainEventSender.send(event2);

        StreamInfo.XInfoStream info = stringRedisTemplate.opsForStream().info(mryRedisProperties.getDomainEventStream());
        assertTrue(info.streamLength() >= 2);

        domainEventJobs.removeOldDomainEventsFromRedis(1, false);
        StreamInfo.XInfoStream updatedInfo = stringRedisTemplate.opsForStream().info(mryRedisProperties.getDomainEventStream());
        assertEquals(1, updatedInfo.streamLength());
    }

    @Test
    public void should_remove_old_webhook_events_from_redis() {
        QrCreatedEvent event1 = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);
        QrCreatedEvent event2 = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);

        redisWebhookEventSender.send(event1);
        redisWebhookEventSender.send(event2);

        StreamInfo.XInfoStream info = stringRedisTemplate.opsForStream().info(mryRedisProperties.getWebhookStream());
        assertTrue(info.streamLength() >= 2);

        domainEventJobs.removeOldWebhookEventsFromRedis(1, false);
        StreamInfo.XInfoStream updatedInfo = stringRedisTemplate.opsForStream().info(mryRedisProperties.getWebhookStream());
        assertEquals(1, updatedInfo.streamLength());
    }

    @Test
    public void should_remove_old_notification_events_from_redis() {
        QrCreatedEvent event1 = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);
        QrCreatedEvent event2 = new QrCreatedEvent(QR.newQrId(), Plate.newPlateId(), Group.newGroupId(), App.newAppId(), NOUSER);

        redisNotificationDomainEventSender.send(event1);
        redisNotificationDomainEventSender.send(event2);

        StreamInfo.XInfoStream info = stringRedisTemplate.opsForStream().info(mryRedisProperties.getNotificationStream());
        assertTrue(info.streamLength() >= 2);

        domainEventJobs.removeOldNotificationEventsFromRedis(1, false);
        StreamInfo.XInfoStream updatedInfo = stringRedisTemplate.opsForStream().info(mryRedisProperties.getNotificationStream());
        assertEquals(1, updatedInfo.streamLength());
    }

}
