package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.service.DefinitionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ParticipantStepValidator implements ConstraintValidator<ValidParticipantStep, ParticipantStep> {

    private final DefinitionService definitionService;

    private String message;

    public ParticipantStepValidator(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @Override
    public void initialize(ValidParticipantStep constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ParticipantStep participantStep, ConstraintValidatorContext context) {

        boolean isValid;

        if ( participantStep == null ) {

            // Will not validate if participant step is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {
                definitionService.getParticipantStepById(participantStep.getId());
                isValid = true;
            } catch ( DataNotFoundException e ) {
                isValid = false;
            }

        }

        return isValid;
    }

}