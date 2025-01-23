package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.service.InstitutionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InstitutionNameValidator implements ConstraintValidator<UniqueInstitutionName, String> {

    private final InstitutionService institutionService;

    private String message;

    public InstitutionNameValidator(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @Override
    public void initialize(UniqueInstitutionName constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {

        boolean isValid;

        if ( name == null ) {

            // Will not validate if name is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {
                institutionService.getInstitutionByName(name);
                isValid = false;
            } catch ( DataNotFoundException e ) {
                isValid = true;
            }

        }

        return isValid;
    }

}