package com.mryqr.core.common.validation.id.attribute;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.apache.commons.codec.binary.Base64.isBase64;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

public class AttributeIdValidator implements ConstraintValidator<AttributeId, String> {

    @Override
    public void initialize(AttributeId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String controlId, ConstraintValidatorContext context) {
        if (isBlank(controlId)) {
            return true;
        }

        if (!startsWith(controlId, "a_")) {
            return false;
        }

        if (controlId.length() < 22 || controlId.length() > 24) {
            return false;
        }

        return isBase64(controlId.substring(2));
    }


}
