package com.mryqr.core.common.validation.id.department;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class DepartmentIdValidator implements ConstraintValidator<DepartmentId, String> {

    private static final Pattern PATTERN = Pattern.compile("^DPT[0-9]{17,19}$");

    @Override
    public void initialize(DepartmentId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String departmentId, ConstraintValidatorContext context) {
        if (isBlank(departmentId)) {
            return true;
        }

        return isDepartmentId(departmentId);
    }

    public static boolean isDepartmentId(String departmentId) {
        return PATTERN.matcher(departmentId).matches();
    }

}
