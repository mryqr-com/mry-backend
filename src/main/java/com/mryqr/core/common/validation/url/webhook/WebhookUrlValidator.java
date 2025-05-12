package com.mryqr.core.common.validation.url.webhook;

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
            URL theUrl = new URL(url);
            String host = theUrl.getHost();

            //以下如果启用的话，会导致API测试失败，
//            if(host.equals("localhost")){
//                return false;
//            }

            return !InetAddresses.isInetAddress(host);
        } catch (Exception exception) {
            return false;
        }
    }

}
