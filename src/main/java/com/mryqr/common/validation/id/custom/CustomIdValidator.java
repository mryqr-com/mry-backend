package com.mryqr.common.validation.id.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.mryqr.common.utils.MryConstants.MAX_CUSTOM_ID_LENGTH;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class CustomIdValidator implements ConstraintValidator<CustomId, String> {

    @Override
    public void initialize(CustomId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String customId, ConstraintValidatorContext context) {
        if (isBlank(customId)) {
            return true;
        }

        if (customId.contains(" ")) {
            return false;
        }

        return customId.length() <= MAX_CUSTOM_ID_LENGTH;
    }


}
