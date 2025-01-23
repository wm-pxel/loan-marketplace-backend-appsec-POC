package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StateValidator.class)
@Documented
public @interface ValidState {

    String message() default "The supplied state is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}