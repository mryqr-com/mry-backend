package com.mryqr.core.common.properties;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties("mry.common")
public class CommonProperties {
    private boolean httpsEnabled;

    @NotBlank
    private String baseDomainName;

    private boolean limitRate;

    @NotBlank
    private String webhookUserName;

    @NotBlank
    private String webhookPassword;

}
