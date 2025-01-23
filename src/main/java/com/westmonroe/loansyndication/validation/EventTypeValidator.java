package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.event.EventType;
import com.westmonroe.loansyndication.service.DefinitionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventTypeValidator implements ConstraintValidator<ValidEventType, EventType> {

    private final DefinitionService definitionService;

    private String message;

    public EventTypeValidator(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @Override
    public void initialize(ValidEventType constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(EventType eventType, ConstraintValidatorContext context) {

        boolean isValid;

        if ( eventType == null ) {

            // Will not validate if event type is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {
                definitionService.getEventTypeById(eventType.getId());
                isValid = true;
            } catch ( DataNotFoundException e ) {
                isValid = false;
            }

        }

        return isValid;
    }

}