package com.mryqr.common.webhook.consume;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.profile.NonCiProfile;
import com.mryqr.common.properties.MryRedisProperties;
import com.mryqr.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.util.ErrorHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.IntStream;

import static com.mryqr.common.utils.MryConstants.REDIS_WEBHOOK_CONSUMER_GROUP;
import static org.springframework.data.redis.connection.stream.Consumer.from;
import static org.springframework.data.redis.connection.stream.ReadOffset.lastConsumed;
import static org.springframework.data.redis.connection.stream.StreamOffset.create;

@Slf4j
@NonCiProfile
@Configuration
@DependsOn("redisStreamInitializer")
@RequiredArgsConstructor
public class RedisWebhookEventConsumeConfiguration {
    private final MryRedisProperties mryRedisProperties;
    private final MryObjectMapper mryObjectMapper;
    private final WebhookEventConsumer webhookEventConsumer;

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> webhookEventListenerContainer(RedisConnectionFactory factory) {
        var options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10)
                .executor(new SimpleAsyncTaskExecutor("mry-webhook-"))
                .targetType(String.class)
                .errorHandler(new MryRedisErrorHandler())
                .build();

        var container = StreamMessageListenerContainer.create(factory, options);

        IntStream.range(1, 2).forEach(index -> {
            try {
                container.receiveAutoAck(
                        from(REDIS_WEBHOOK_CONSUMER_GROUP, InetAddress.getLocalHost().getHostName() + "-" + index),
                        create(mryRedisProperties.getWebhookStream(), lastConsumed()),
                        message -> {
                            String jsonString = message.getValue();
                            DomainEvent domainEvent = mryObjectMapper.readValue(jsonString, DomainEvent.class);
                            try {
                                webhookEventConsumer.consume(domainEvent);
                            } catch (Throwable t) {
                                log.error("Failed to listen webhook event[{}:{}].", domainEvent.getType(), domainEvent.getId(), t);
                            }
                        });
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });

        container.start();
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

