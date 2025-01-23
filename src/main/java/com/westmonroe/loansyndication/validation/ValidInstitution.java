package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InstitutionValidator.class)
@Documented
public @interface ValidInstitution {

    String message() default "The supplied institution is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}