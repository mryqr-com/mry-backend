package com.mryqr.core.common.validation.nospace;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class NoSpaceValidator implements ConstraintValidator<NoSpace, String> {

    @Override
    public void initialize(NoSpace constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value)) {
            return true;
        }

        return !value.contains(" ");
    }
}
