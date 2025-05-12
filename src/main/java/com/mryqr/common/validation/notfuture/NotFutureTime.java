package com.mryqr.common.validation.notfuture;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = NotFutureTimeValidator.class)
@Documented
public @interface NotFutureTime {
    String message() default "Must not be future time.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
