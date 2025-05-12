package com.mryqr.common.event.consume.infrastructure;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.event.consume.ConsumingDomainEvent;
import com.mryqr.common.event.consume.DomainEventConsumer;
import com.mryqr.common.profile.NonCiProfile;
import com.mryqr.common.properties.MryRedisProperties;
import com.mryqr.common.tracing.MryTracingService;
import com.mryqr.common.utils.MryObjectMapper;
import io.micrometer.tracing.ScopedSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.util.ErrorHandler;

import static com.mryqr.common.utils.MryConstants.REDIS_DOMAIN_EVENT_CONSUMER_GROUP;
import static org.springframework.data.redis.connection.stream.Consumer.from;
import static org.springframework.data.redis.connection.stream.ReadOffset.lastConsumed;
import static org.springframework.data.redis.connection.stream.StreamOffset.create;

@Slf4j
@Configuration
@NonCiProfile
@RequiredArgsConstructor
@DependsOn("redisStreamInitializer")
@ConditionalOnProperty(value = "mry.redis.domainEventStreamEnabled", havingValue = "true")
public class RedisDomainEventConsumeConfiguration {
    private final MryRedisProperties mryRedisProperties;
    private final MryObjectMapper mryObjectMapper;
    private final DomainEventConsumer<DomainEvent> domainEventConsumer;
    private final MryTracingService mryTracingService;

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> domainEventContainer(RedisConnectionFactory factory) {
        var options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(20)
                .executor(new SimpleAsyncTaskExecutor("mry-event-"))
                .targetType(String.class)
                .errorHandler(new MryRedisErrorHandler())
                .build();

        var container = StreamMessageListenerContainer.create(factory, options);

        mryRedisProperties.allDomainEventStreams().forEach(stream -> {
            container.receiveAutoAck(
                    from(REDIS_DOMAIN_EVENT_CONSUMER_GROUP, "DomainEventRedisStreamConsumer-" + stream),
                    create(stream, lastConsumed()),
                    message -> {
                        ScopedSpan scopedSpan = mryTracingService.startNewSpan("domain-event-listener");

                        String jsonString = message.getValue();
                        DomainEvent domainEvent = mryObjectMapper.readValue(jsonString, DomainEvent.class);
                        try {
                            domainEventConsumer.consume(new ConsumingDomainEvent<>(domainEvent.getId(), domainEvent.getType().name(), domainEvent));
                        } catch (Throwable t) {
                            log.error("Failed to listen domain event[{}:{}].", domainEvent.getType(), domainEvent.getId(), t);
                        }

                        scopedSpan.end();
                    });
        });

        container.start();
        log.info("Start consuming domain events from redis stream.");
        return container;
    }

    @Slf4j
    private static class MryRedisErrorHandler implements ErrorHandler {
        @Override
        public void handleError(Throwable t) {
            log.error(t.getMessage(), t);
        }
    }
}

