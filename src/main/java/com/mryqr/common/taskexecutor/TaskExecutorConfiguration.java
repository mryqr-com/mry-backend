package com.mryqr.common.taskexecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfiguration {

    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.initialize();
        executor.setThreadNamePrefix("mry-common-");
        return executor;
    }

    @Bean
    public TaskExecutor qrAccessCountTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.initialize();
        executor.setThreadNamePrefix("access-qr-");
        return executor;
    }

    @Bean
    public TaskExecutor consumeDomainEventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.initialize();
        executor.setThreadNamePrefix("mry-event-");
        return executor;
    }

    @Bean
    public TaskExecutor sendWebhookTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.initialize();
        executor.setThreadNamePrefix("mry-webhook-");
        return executor;
    }

    @Bean
    public TaskExecutor sendNotificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.initialize();
        executor.setThreadNamePrefix("mry-notify-");
        return executor;
    }

}
