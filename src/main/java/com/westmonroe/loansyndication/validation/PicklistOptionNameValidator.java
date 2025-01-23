package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.service.PicklistService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PicklistOptionNameValidator implements ConstraintValidator<ValidPicklistOptionName, String> {

    private final PicklistService picklistService;

    private String category;
    private String message;

    public PicklistOptionNameValidator(PicklistService picklistService) {
        this.picklistService = picklistService;
    }

    @Override
    public void initialize(ValidPicklistOptionName constraintAnnotation) {
        this.category = constraintAnnotation.category();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String optionName, ConstraintValidatorContext context) {

        boolean result;

        if ( optionName == null ) {

            // Will not validate if picklistItem is null.  For fields that are required, add the @NotNull annotation.
            result = true;

        } else {

            // Validate non-empty picklistItem.
            result = picklistService.validPicklistOptionForCategory(optionName, category);

        }

        return result;
    }

}