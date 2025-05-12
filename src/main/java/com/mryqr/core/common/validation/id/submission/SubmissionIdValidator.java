package com.mryqr.core.common.validation.id.submission;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class SubmissionIdValidator implements ConstraintValidator<SubmissionId, String> {

    private static final Pattern PATTERN = Pattern.compile("^SBM[0-9]{17,19}$");

    @Override
    public void initialize(SubmissionId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String submissionId, ConstraintValidatorContext context) {
        if (isBlank(submissionId)) {
            return true;
        }

        return PATTERN.matcher(submissionId).matches();
    }

}
