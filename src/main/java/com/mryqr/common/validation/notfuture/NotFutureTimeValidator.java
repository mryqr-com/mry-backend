package com.mryqr.common.validation.notfuture;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.temporal.Temporal;

import static java.time.Instant.from;
import static java.time.Instant.now;

public class NotFutureTimeValidator implements ConstraintValidator<NotFutureTime, Temporal> {

    @Override
    public void initialize(NotFutureTime constraintAnnotation) {
    }

    @Override
    public boolean isValid(Temporal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !from(value).isAfter(now());
    }
}
