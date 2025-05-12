package com.mryqr.common.validation.collection;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoNullElementValidator.class)
@Documented
public @interface NoNullElement {

    String message() default "Collection must not contain null element.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
