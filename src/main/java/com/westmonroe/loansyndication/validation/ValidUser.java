package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserValidator.class)
@Documented
public @interface ValidUser {

    String message() default "The supplied user is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}