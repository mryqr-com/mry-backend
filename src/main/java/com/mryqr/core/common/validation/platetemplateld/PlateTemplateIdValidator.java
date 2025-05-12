package com.mryqr.core.common.validation.platetemplateld;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PlateTemplateIdValidator implements ConstraintValidator<PlateTemplateId, String> {

    private static final Pattern PATTERN = Pattern.compile("^PT[0-9]{17,19}$");

    @Override
    public void initialize(PlateTemplateId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String templateId, ConstraintValidatorContext context) {
        if (isBlank(templateId)) {
            return true;
        }

        return PATTERN.matcher(templateId).matches();
    }

}
