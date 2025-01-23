package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.service.DefinitionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventTypeNameValidator implements ConstraintValidator<ValidEventTypeName, String> {

    private final DefinitionService definitionService;

    private String message;

    public EventTypeNameValidator(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @Override
    public void initialize(ValidEventTypeName constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String eventTypeName, ConstraintValidatorContext context) {

        boolean isValid;

        if ( eventTypeName == null ) {

            // Will not validate if event type is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {
                definitionService.getEventTypeByName(eventTypeName);
                isValid = true;
            } catch ( DataNotFoundException e ) {
                isValid = false;
            }

        }

        return isValid;
    }

}