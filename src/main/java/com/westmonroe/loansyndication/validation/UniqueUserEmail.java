package com.westmonroe.loansyndication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserEmailValidator.class)
@Documented
public @interface UniqueUserEmail {

    String message() default "This email is already assigned to a user in Lamina";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
