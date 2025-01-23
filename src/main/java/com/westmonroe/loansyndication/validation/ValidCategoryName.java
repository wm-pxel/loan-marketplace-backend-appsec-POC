package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryNameValidator.class)
@Documented
public @interface ValidCategoryName {

    String message() default "The supplied category name is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}