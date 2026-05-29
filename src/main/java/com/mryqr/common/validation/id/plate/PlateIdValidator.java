package com.mryqr.common.validation.id.plate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PlateIdValidator implements ConstraintValidator<PlateId, String> {

    private static final Pattern PATTERN = Pattern.compile("^MRY[0-9]{17,19}$");

    @Override
    public void initialize(PlateId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String plateId, ConstraintValidatorContext context) {
        if (isBlank(plateId)) {
            return true;
        }

        return isPlateId(plateId);
    }

    public static boolean isPlateId(String plateId) {
        return PATTERN.matcher(plateId).matches();
    }

}
