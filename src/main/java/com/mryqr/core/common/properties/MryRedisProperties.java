package com.mryqr.core.common.properties;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties("mry.redis")
public class MryRedisProperties {

    @NotBlank
    private String domainEventStream;

    @NotBlank
    private String notificationStream;

    @NotBlank
    private String webhookStream;

}
