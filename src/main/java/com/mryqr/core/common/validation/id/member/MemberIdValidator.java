package com.mryqr.core.common.validation.id.member;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MemberIdValidator implements ConstraintValidator<MemberId, String> {

    private static final Pattern PATTERN = Pattern.compile("^MBR[0-9]{17,19}$");

    @Override
    public void initialize(MemberId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String memberId, ConstraintValidatorContext context) {
        if (isBlank(memberId)) {
            return true;
        }

        return isMemberId(memberId);
    }

    public static boolean isMemberId(String memberId) {
        return PATTERN.matcher(memberId).matches();
    }

}
