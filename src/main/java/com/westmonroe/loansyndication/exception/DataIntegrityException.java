package com.westmonroe.loansyndication.exception;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.Constants.GQL_CLASSIFICATION;
import static com.westmonroe.loansyndication.utils.Constants.GQL_HTTP_STATUS;

@ResponseStatus(HttpStatus.CONFLICT)
public class DataIntegrityException extends RuntimeException implements GraphQLError {

    public DataIntegrityException() {
        super();
    }

    public DataIntegrityException(String message) {
        super(message);
    }

    public DataIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataIntegrityException(Throwable cause) {
        super(cause);
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
            GQL_HTTP_STATUS, this.getClass().getAnnotation(ResponseStatus.class).value()
        );
    }

}