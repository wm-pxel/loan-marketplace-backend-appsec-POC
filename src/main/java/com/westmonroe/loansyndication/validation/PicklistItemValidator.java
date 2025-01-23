package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.service.PicklistService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PicklistItemValidator implements ConstraintValidator<ValidPicklistItem, PicklistItem> {

    private final PicklistService picklistService;

    private String category;
    private String message;

    public PicklistItemValidator(PicklistService picklistService) {
        this.picklistService = picklistService;
    }

    @Override
    public void initialize(ValidPicklistItem constraintAnnotation) {
        this.category = constraintAnnotation.category();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(PicklistItem picklistItem, ConstraintValidatorContext context) {

        boolean result;

        if ( picklistItem == null ) {

            // Will not validate if picklistItem is null.  For fields that are required, add the @NotNull annotation.
            result = true;

        } else {

            // Validate non-empty picklistItem.
            result = picklistService.validPicklistIdForCategory(picklistItem.getId(), category);

        }

        return result;
    }

}