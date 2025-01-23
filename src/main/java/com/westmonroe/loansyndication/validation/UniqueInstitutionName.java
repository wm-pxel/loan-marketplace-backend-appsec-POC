package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InstitutionNameValidator.class)
@Documented
public @interface UniqueInstitutionName {

    String message() default "The institution name is already in use";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}