package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventTypeValidator.class)
@Documented
public @interface ValidEventType {

    String message() default "The supplied event type is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}