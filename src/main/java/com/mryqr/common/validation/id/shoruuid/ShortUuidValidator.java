package com.mryqr.common.validation.id.shoruuid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.apache.commons.codec.binary.Base64.isBase64;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ShortUuidValidator implements ConstraintValidator<ShortUuid, String> {

    @Override
    public void initialize(ShortUuid constraintAnnotation) {
    }

    @Override
    public boolean isValid(String uuid, ConstraintValidatorContext context) {
        if (isBlank(uuid)) {
            return true;
        }

        if (uuid.length() < 20 || uuid.length() > 23) {
            return false;
        }

        return isBase64(uuid);
    }


}
