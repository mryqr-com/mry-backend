package com.mryqr.common.validation.id.assignment;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class AssignmentIdValidator implements ConstraintValidator<AssignmentId, String> {

    private static final Pattern PATTERN = Pattern.compile("^ASM[0-9]{17,19}$");

    @Override
    public void initialize(AssignmentId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String assignmentId, ConstraintValidatorContext context) {
        if (isBlank(assignmentId)) {
            return true;
        }

        return isAssignmentId(assignmentId);
    }

    public static boolean isAssignmentId(String assignmentId) {
        return PATTERN.matcher(assignmentId).matches();
    }

}
