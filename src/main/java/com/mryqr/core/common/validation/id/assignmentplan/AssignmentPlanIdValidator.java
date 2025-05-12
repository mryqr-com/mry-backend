package com.mryqr.core.common.validation.id.assignmentplan;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class AssignmentPlanIdValidator implements ConstraintValidator<AssignmentPlanId, String> {

    private static final Pattern PATTERN = Pattern.compile("^ASP[0-9]{17,19}$");

    @Override
    public void initialize(AssignmentPlanId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String assignmentPlanId, ConstraintValidatorContext context) {
        if (isBlank(assignmentPlanId)) {
            return true;
        }

        return isAssignmentPlanId(assignmentPlanId);
    }

    public static boolean isAssignmentPlanId(String assignmentPlanId) {
        return PATTERN.matcher(assignmentPlanId).matches();
    }

}
