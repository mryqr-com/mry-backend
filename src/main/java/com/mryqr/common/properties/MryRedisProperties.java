package com.mryqr.common.properties;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
@Component
@Validated
@ConfigurationProperties("mry.redis")
public class MryRedisProperties {

    @NotBlank
    private String domainEventStreamPrefix;

    @Min(value = 1)
    @Max(value = 10)
    private int domainEventStreamCount;


    @NotBlank
    private String notificationStream;

    @NotBlank
    private String webhookStream;

    public List<String> allDomainEventStreams() {
        return IntStream.range(1, this.domainEventStreamCount + 1).mapToObj(this::domainEventStreamOfIndex).toList();
    }

    public String domainEventStreamForTenant(String tenantId) {
        if (isBlank(tenantId)) {
            return this.domainEventStreamOfIndex(1);
        }

        int index = Math.abs(tenantId.hashCode() % this.domainEventStreamCount) + 1;
        return this.domainEventStreamOfIndex(index);
    }

    private String domainEventStreamOfIndex(int index) {
        return this.domainEventStreamPrefix + "." + index;
    }

}
