package com.mryqr.common.validation.id.platebatch;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PlateBatchIdValidator implements ConstraintValidator<PlateBatchId, String> {

    private static final Pattern PATTERN = Pattern.compile("^BCH[0-9]{17,19}$");

    @Override
    public void initialize(PlateBatchId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String batchId, ConstraintValidatorContext context) {
        if (isBlank(batchId)) {
            return true;
        }

        return PATTERN.matcher(batchId).matches();
    }

}
