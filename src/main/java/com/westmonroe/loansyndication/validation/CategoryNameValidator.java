package com.westmonroe.loansyndication.validation;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.DocumentCategory;
import com.westmonroe.loansyndication.service.DefinitionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CategoryNameValidator implements ConstraintValidator<ValidCategoryName, String> {

    private final DefinitionService definitionService;

    private String message;

    public CategoryNameValidator(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @Override
    public void initialize(ValidCategoryName constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String categoryName, ConstraintValidatorContext context) {

        boolean isValid;

        if ( categoryName == null ) {

            // Will not validate if user is null.  For fields that are required, add the @NotNull annotation.
            isValid = true;

        } else {

            try {

                DocumentCategory category = definitionService.getDocumentCategoryByName(categoryName);

                if ( category.getDealDocumentFlag().equals("Y") ) {
                    isValid = true;     // VALID: Is a valid deal document category.
                } else {
                    isValid = false;    // INVALID: Valid category but not one of the deal document categories.
                }

            } catch ( DataNotFoundException e ) {
                isValid = false;
            }

        }

        return isValid;
    }

}