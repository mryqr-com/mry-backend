package com.mryqr.common.validation.collection;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@SuppressWarnings("rawtypes")
public class NoNullElementValidator implements ConstraintValidator<NoNullElement, Collection> {


    @Override
    public void initialize(NoNullElement constraintAnnotation) {
    }

    @Override
    public boolean isValid(Collection collection, ConstraintValidatorContext constraintValidatorContext) {
        if (isEmpty(collection)) {
            return true;
        }

        return !collection.contains(null);
    }

}
