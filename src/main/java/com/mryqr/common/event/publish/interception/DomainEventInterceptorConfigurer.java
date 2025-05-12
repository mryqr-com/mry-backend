package com.mryqr.common.event.publish.interception;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DomainEventInterceptorConfigurer implements WebMvcConfigurer {
    private final DomainEventHandlingInterceptor domainEventHandlingInterceptor;

    public DomainEventInterceptorConfigurer(DomainEventHandlingInterceptor interceptor) {
        this.domainEventHandlingInterceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(domainEventHandlingInterceptor);
    }
}
