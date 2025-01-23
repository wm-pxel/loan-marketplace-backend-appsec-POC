package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserValidator implements ConstraintValidator<ValidUser, User> {

    private final UserService userService;

    private String message;

    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(ValidUser constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext context) {

        boolean isValid;

        if ( user == null ) {

            // Will not validate if user is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {
                userService.getUserByUid(user.getUid());
                isValid = true;
            } catch ( DataNotFoundException e ) {
                isValid = false;
            }

        }

        return isValid;
    }

}