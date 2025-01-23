package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ParticipantStepValidator.class)
@Documented
public @interface ValidParticipantStep {

    String message() default "The supplied participant step is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}