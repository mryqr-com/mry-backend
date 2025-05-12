package com.mryqr.common.validation.url.webhook;

import com.google.common.net.InetAddresses;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URL;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class WebhookUrlValidator implements ConstraintValidator<WebhookUrl, String> {

    @Override
    public void initialize(WebhookUrl constraintAnnotation) {
    }

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if (isBlank(url)) {
            return true;
        }

        try {
            return !InetAddresses.isInetAddress(new URL(url).getHost());
        } catch (Exception exception) {
            return false;
        }
    }

}
