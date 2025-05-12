package com.mryqr.common.validation.url.http;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.mryqr.common.utils.CommonUtils.isValidUrl;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class HttpUrlValidator implements ConstraintValidator<HttpUrl, String> {

    @Override
    public void initialize(HttpUrl constraintAnnotation) {
    }

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if (isBlank(url)) {
            return true;
        }

        if (!isValidUrl(url)) {
            return false;
        }

        String lowerCaseUrl = url.toLowerCase();
        return lowerCaseUrl.startsWith("http") || lowerCaseUrl.startsWith("https");
    }

}
