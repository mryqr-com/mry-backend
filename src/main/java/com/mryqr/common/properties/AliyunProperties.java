package com.mryqr.common.properties;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties("mry.aliyun")
public class AliyunProperties {
    @NotBlank
    private String ak;

    @NotBlank
    private String aks;

    @NotBlank
    private String role;

    @NotBlank
    private String ossBucket;

    @NotBlank
    private String ossEndpoint;

    @NotBlank
    private String smsSignName;

    @NotBlank
    private String smsTemplateCode;

    @NotBlank
    private String deliveryQueryAppCode;

    @NotBlank
    private String ossUtilCommand;

    @NotBlank
    private String ossUtilConfigFile;

}
