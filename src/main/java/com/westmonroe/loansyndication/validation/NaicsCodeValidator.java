package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.NaicsCode;
import com.westmonroe.loansyndication.service.DefinitionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NaicsCodeValidator implements ConstraintValidator<ValidNaicsCode, Object> {

    private final DefinitionService definitionService;
    private String message;

    public NaicsCodeValidator(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @Override
    public void initialize(ValidNaicsCode constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object naicsCode, ConstraintValidatorContext context) {

        boolean isValid;

        if ( naicsCode == null ) {

            // Will not validate if event type is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {

                if ( naicsCode instanceof String ) {
                    definitionService.getNaicsCodeByCode((String) naicsCode);
                } else  if ( naicsCode instanceof NaicsCode ) {
                    definitionService.getNaicsCodeByCode(((NaicsCode) naicsCode).getCode());
                }

                isValid = true;

            } catch ( DataNotFoundException e ) {
                isValid = false;
            }

        }

        return isValid;
    }

}