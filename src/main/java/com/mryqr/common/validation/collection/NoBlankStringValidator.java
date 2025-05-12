package com.mryqr.common.validation.collection;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public class NoBlankStringValidator implements ConstraintValidator<NoBlankString, Collection<String>> {


    @Override
    public void initialize(NoBlankString parameters) {
    }

    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext context) {
        if (isEmpty(collection)) {
            return true;
        }

        return collection.stream().noneMatch(StringUtils::isBlank);
    }

}
