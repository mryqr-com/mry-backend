package com.mryqr.common.validation.color;

import com.mryqr.common.utils.MryRegexConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ColorValidator implements ConstraintValidator<Color, String> {
    private static final Pattern RGBA_COLOR_PATTERN = Pattern.compile(MryRegexConstants.RGBA_COLOR_PATTERN, CASE_INSENSITIVE);
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(MryRegexConstants.HEX_COLOR_PATTERN, CASE_INSENSITIVE);
    private static final Pattern RGB_COLOR_PATTERN = Pattern.compile(MryRegexConstants.RGB_COLOR_PATTERN, CASE_INSENSITIVE);

    @Override
    public void initialize(Color constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value)) {
            return true;
        }

        return RGBA_COLOR_PATTERN.matcher(value).matches() ||
               HEX_COLOR_PATTERN.matcher(value).matches() ||
               RGB_COLOR_PATTERN.matcher(value).matches();
    }
}
