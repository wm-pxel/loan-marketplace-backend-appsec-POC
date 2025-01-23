package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.service.DefinitionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StateValidator implements ConstraintValidator<ValidState, String> {

    private final DefinitionService definitionService;

    private String message;

    public StateValidator(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @Override
    public void initialize(ValidState constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String state, ConstraintValidatorContext context) {

        boolean isValid;

        if ( state == null ) {

            // Will not validate if state is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {
                definitionService.getStateByCode(state);
                isValid = true;
            } catch ( DataNotFoundException e ) {
                isValid = false;
            }

        }

        return isValid;
    }

}