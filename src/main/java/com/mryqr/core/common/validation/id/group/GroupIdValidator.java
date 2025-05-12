package com.mryqr.core.common.validation.id.group;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class GroupIdValidator implements ConstraintValidator<GroupId, String> {

    private static final Pattern PATTERN = Pattern.compile("^GRP[0-9]{17,19}$");

    @Override
    public void initialize(GroupId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String groupId, ConstraintValidatorContext context) {
        if (isBlank(groupId)) {
            return true;
        }

        return isGroupId(groupId);
    }

    public static boolean isGroupId(String groupId) {
        return PATTERN.matcher(groupId).matches();
    }

}
