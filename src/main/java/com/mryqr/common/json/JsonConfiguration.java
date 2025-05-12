package com.mryqr.common.json;

import com.mryqr.common.utils.MryObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfiguration {

    @Bean
    public MryObjectMapper objectMapper() {
        return new MryObjectMapper();
    }
}
