package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.service.InstitutionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InstitutionValidator implements ConstraintValidator<ValidInstitution, Institution> {

    private final InstitutionService institutionService;

    private String message;

    public InstitutionValidator(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @Override
    public void initialize(ValidInstitution constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public boolean isValid(Institution institution, ConstraintValidatorContext context) {

        boolean isValid;

        if ( institution == null ) {

            // Will not validate if institution is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {
                institutionService.getInstitutionByUid(institution.getUid());
                isValid = true;
            } catch ( DataNotFoundException e ) {
                isValid = false;
            }

        }

        return isValid;
    }

}