package com.mryqr.common.validation.id.page;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.apache.commons.codec.binary.Base64.isBase64;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

public class PageIdValidator implements ConstraintValidator<PageId, String> {

    @Override
    public void initialize(PageId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String pageId, ConstraintValidatorContext context) {
        if (isBlank(pageId)) {
            return true;
        }

        if (!startsWith(pageId, "p_")) {
            return false;
        }

        if (pageId.length() < 22 || pageId.length() > 24) {
            return false;
        }
        return isBase64(pageId.substring(2));
    }


}
