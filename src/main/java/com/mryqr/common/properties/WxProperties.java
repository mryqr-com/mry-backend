package com.mryqr.common.properties;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties("mry.wx")
public class WxProperties {

    private boolean mobileWxEnabled;

    @NotBlank
    private String mobileAppId;

    @NotBlank
    private String mobileAppSecret;

    @NotBlank
    private String pcAppId;

    @NotBlank
    private String pcAppSecret;

    @NotBlank
    private String submissionCreatedTemplateId;

    @NotBlank
    private String submissionUpdatedTemplateId;

    @NotBlank
    private String submissionApprovedTemplateId;

    @NotBlank
    private String assignmentNearExpireTemplateId;

}
