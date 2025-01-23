package com.westmonroe.loansyndication.exception;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.Constants.*;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ValidationException extends RuntimeException implements GraphQLError {

    private List<FieldError> errors = new ArrayList<>();

    public ValidationException() {
        super();
    }

    public ValidationException(List<FieldError> errors) {
        super();
        this.errors = errors;
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<FieldError> errors) {
        super(message);
        this.errors = errors;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message, List<FieldError> errors, Throwable cause) {
        super(message, cause);
        this.errors = errors;
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(List<FieldError> errors, Throwable cause) {
        super(cause);
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return Collections.emptyList();
    }

    @Override
    public ErrorClassification getErrorType() {
        return null;
    }

    @Override
    public List<Object> getPath() {
        return GraphQLError.super.getPath();
    }

    @Override
    public Map<String, Object> toSpecification() {
        return GraphQLError.super.toSpecification();
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Map.of(
            GQL_CLASSIFICATION, this.getClass().getSimpleName(),
            GQL_STATUS_CODE, this.getClass().getAnnotation(ResponseStatus.class).value(),
            GQL_VALIDATION_ERRORS, errors
        );
    }

}