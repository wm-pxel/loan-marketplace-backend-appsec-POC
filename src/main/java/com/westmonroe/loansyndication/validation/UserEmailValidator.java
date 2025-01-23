package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserEmailValidator implements ConstraintValidator<UniqueUserEmail, String> {

    private final UserService userService;

    private String message;

    public UserEmailValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(UniqueUserEmail constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        boolean isValid;

        if ( email == null ) {

            // Will not validate if email is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {
                userService.getUserByEmail(email);
                isValid = false;
            } catch ( DataNotFoundException e ) {
                isValid = true;
            }

        }

        return isValid;
    }

}