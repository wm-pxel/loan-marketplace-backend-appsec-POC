package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PicklistItemValidator.class)
@Documented
public @interface ValidPicklistItem {

    String category() default "";

    String message() default "Invalid picklist item for {category}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}