package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PicklistOptionNameValidator.class)
@Documented
public @interface ValidPicklistOptionName {

    String category() default "";

    String message() default "Invalid picklist option name for {category}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}