package com.mryqr.common.validation.id.qr;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class QrIdValidator implements ConstraintValidator<QrId, String> {

    private static final Pattern PATTERN = Pattern.compile("^QRC[0-9]{17,19}$");

    @Override
    public void initialize(QrId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String qrId, ConstraintValidatorContext context) {
        if (isBlank(qrId)) {
            return true;
        }

        return isQrId(qrId);
    }

    public static boolean isQrId(String qrId) {
        return PATTERN.matcher(qrId).matches();
    }

}
