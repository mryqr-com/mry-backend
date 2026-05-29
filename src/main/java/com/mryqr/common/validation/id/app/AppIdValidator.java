package com.mryqr.common.validation.id.app;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class AppIdValidator implements ConstraintValidator<AppId, String> {

    private static final Pattern PATTERN = Pattern.compile("^APP[0-9]{17,19}$");

    @Override
    public void initialize(AppId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String appId, ConstraintValidatorContext context) {
        if (isBlank(appId)) {
            return true;
        }

        return isAppId(appId);
    }

    public static boolean isAppId(String appId) {
        return PATTERN.matcher(appId).matches();
    }

}
