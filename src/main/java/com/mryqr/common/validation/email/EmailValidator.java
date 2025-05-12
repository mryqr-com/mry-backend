package com.mryqr.common.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.mryqr.common.utils.MryRegexConstants.EMAIL_PATTERN;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class EmailValidator implements ConstraintValidator<Email, String> {

    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN, CASE_INSENSITIVE);

    @Override
    public void initialize(Email constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value)) {
            return true;
        }

        return value.length() <= 50 && PATTERN.matcher(value).matches();
    }
}
