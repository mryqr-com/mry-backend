package com.mryqr.common.mongo;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import static com.mongodb.ReadPreference.secondaryPreferred;
import static com.mongodb.WriteConcern.MAJORITY;
import static org.springframework.data.mongodb.core.WriteResultChecking.EXCEPTION;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> {
            builder.applyToConnectionPoolSettings(poolBuilder -> {
                poolBuilder.maxSize(500).minSize(5);
            });
        };
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoConverter converter) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, converter);
        mongoTemplate.setWriteConcern(MAJORITY);
        mongoTemplate.setWriteConcernResolver(action -> MAJORITY);
        mongoTemplate.setWriteResultChecking(EXCEPTION);
        mongoTemplate.setReadPreference(secondaryPreferred());
        return mongoTemplate;
    }

}
