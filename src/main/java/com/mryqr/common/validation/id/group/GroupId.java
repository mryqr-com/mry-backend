package com.mryqr.common.validation.id.group;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = GroupIdValidator.class)
@Documented
public @interface GroupId {
    String message() default "Group ID format is incorrect.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
