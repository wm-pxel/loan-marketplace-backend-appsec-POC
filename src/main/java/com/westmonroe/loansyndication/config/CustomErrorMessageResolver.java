package com.westmonroe.loansyndication.config;

import com.westmonroe.loansyndication.model.error.ErrorDetail;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.westmonroe.loansyndication.utils.Constants.GQL_CLASSIFICATION;
import static com.westmonroe.loansyndication.utils.Constants.GQL_VIOLATION;

@Component
public class CustomErrorMessageResolver extends DataFetcherExceptionResolverAdapter {

    public CustomErrorMessageResolver() { }

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {

        List<GraphQLError> errors = Collections.synchronizedList(new ArrayList<>());

        if ( ex instanceof ConstraintViolationException cve ) {

            List<GraphQLError> finalErrors = errors;
            cve.getConstraintViolations().forEach(cv -> {

                Map<String, Object> extensions = new HashMap<>();
                extensions.put(GQL_CLASSIFICATION, ex.getClass().getSimpleName());

                ErrorDetail ed = new ErrorDetail();
                ed.setFieldName(((PathImpl) cv.getPropertyPath()).getLeafNode().getName());
                ed.setMessage(cv.getMessage());
                ed.setRejectedValue(cv.getInvalidValue());
                extensions.put(GQL_VIOLATION, ed);

                GraphQLError error = GraphqlErrorBuilder.newError(env)
                        .message(cv.getMessage())
                        .extensions(extensions)
                        .build();
                finalErrors.add(error);
            });

        } else {

            GraphQLError error = resolveToSingleError(ex, env);
            errors = Collections.singletonList(error);

        }

        return errors;
    }

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        Map<String, Object> extensions = new HashMap<>();

        if ( ex instanceof GraphQLError graphQLError ) {
            extensions = graphQLError.getExtensions();
        } else {
            extensions.put(GQL_CLASSIFICATION, ex.getClass().getSimpleName());
        }

        return GraphqlErrorBuilder.newError(env)
                .message(ex.getMessage())
                .extensions(extensions)
                .build();
    }

}