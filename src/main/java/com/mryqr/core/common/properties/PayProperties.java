package com.mryqr.core.common.properties;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties("mry.pay")
public class PayProperties {
    @NotBlank
    private String wxMerchantId;

    @NotBlank
    private String wxMerchantSerialNumber;

    @NotBlank
    private String wxApiV3Key;

}
