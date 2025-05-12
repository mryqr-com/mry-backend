package com.mryqr.common.validation.id.inappnotification;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class InAppNotificationIdValidator implements ConstraintValidator<InAppNotificationId, String> {

    private static final Pattern PATTERN = Pattern.compile("^IAN[0-9]{17,19}$");

    @Override
    public void initialize(InAppNotificationId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String inAppNotificationId, ConstraintValidatorContext context) {
        if (isBlank(inAppNotificationId)) {
            return true;
        }

        return PATTERN.matcher(inAppNotificationId).matches();
    }

}
