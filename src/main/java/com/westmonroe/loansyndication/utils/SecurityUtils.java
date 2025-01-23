package com.westmonroe.loansyndication.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.model.error.ErrorInfo;
import graphql.GraphqlErrorBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.Constants.GQL_CLASSIFICATION;
import static com.westmonroe.loansyndication.utils.Constants.GQL_HTTP_STATUS;

@Slf4j
public class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("The class cannot be instantiated. It is a utility class.");
    }

    public static void writeErrorResponse(HttpServletRequest request, HttpServletResponse response
            , ObjectMapper objectMapper, String message, String className) throws IOException {

        log.error(message);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        /*
         *  Customize the response based on whether it was a GraphQL or REST API request.
         */
        if ( request.getRequestURI().contains("/graphql") ) {

            // GraphQL errors will ALWAYS have a 200 response.
            response.setStatus(HttpStatus.OK.value());

            response.getWriter().write("{ \"errors\": ["
                    + objectMapper.writeValueAsString(GraphqlErrorBuilder.newError()
                    .message(message)
                    .extensions(Map.of(
                        GQL_CLASSIFICATION, className,
                        GQL_HTTP_STATUS, HttpStatus.UNAUTHORIZED.name()
                    ))
                    .build())
                    + "]}");

        } else {

            // REST API errors will have the appropriate response code.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                objectMapper.writeValueAsString(
                    new ErrorInfo("Authentication Exception", HttpServletResponse.SC_UNAUTHORIZED, Instant.now().toString(), message)
                )
            );

        }

    }

}