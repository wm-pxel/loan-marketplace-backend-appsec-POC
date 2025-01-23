package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NaicsCodeValidator.class)
@Documented
public @interface ValidNaicsCode {

    String message() default "The supplied NAICS code is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}