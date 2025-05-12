package com.mryqr.common.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NotificationServiceConfiguration {

    @Bean
    public CompositeNotificationService compositeNotificationService(List<NotificationService> notificationServices) {
        return new CompositeNotificationService(notificationServices);
    }
}
