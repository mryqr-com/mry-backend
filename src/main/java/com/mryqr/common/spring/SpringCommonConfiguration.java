package com.mryqr.common.spring;

import com.mryqr.core.common.utils.MryObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.TransactionManager;
import org.springframework.web.client.RestTemplate;

import static java.time.Duration.ofSeconds;

@EnableCaching
@EnableAsync
@EnableRetry
@Configuration
public class SpringCommonConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(ofSeconds(10))
                .setReadTimeout(ofSeconds(10))
                .build();
    }

    @Bean
    public MryObjectMapper objectMapper() {
        return new MryObjectMapper();
    }

    @Bean
    public TransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTransactionManager(mongoDatabaseFactory);
    }

}
