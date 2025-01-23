package com.westmonroe.loansyndication.handler;

import com.westmonroe.loansyndication.exception.*;
import com.westmonroe.loansyndication.model.error.ErrorDetail;
import com.westmonroe.loansyndication.model.error.ErrorInfo;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private ResponseEntity<ErrorInfo> createErrorResponse(String title, HttpStatus status, String message) {
        return createErrorResponse(title, status, message, null);
    }

    private ResponseEntity<ErrorInfo> createErrorResponse(String title, HttpStatus status, String message, List<FieldError> errors) {

        ErrorInfo errorInfo = new ErrorInfo(title, status.value(), Instant.now().toString(), message);

        if ( errors != null && !errors.isEmpty() ) {

            List<ErrorDetail> errorDetails = new ArrayList<>();

            for ( FieldError error : errors ) {

                ErrorDetail ed = new ErrorDetail();

                ed.setFieldName(error.getField());
                ed.setMessage(error.getDefaultMessage());
                ed.setRejectedValue(error.getRejectedValue());

                errorDetails.add(ed);
            }

            errorInfo.setErrors(errorDetails);
        }

        return new ResponseEntity<>(errorInfo, status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorInfo> handleException(RuntimeException e) {
        return createErrorResponse("Runtime Exception", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorInfo> handleAccessDeniedException(AccessDeniedException ade) {
        return createErrorResponse("Access Denied Exception", HttpStatus.FORBIDDEN, ade.getMessage());
    }

    @ExceptionHandler(ActivityCreationException.class)
    public ResponseEntity<ErrorInfo> handleAccessDeniedException(ActivityCreationException ace) {
        return createErrorResponse("Activity Creation Exception", HttpStatus.FORBIDDEN, ace.getMessage());
    }

    @ExceptionHandler(AwsS3Exception.class)
    public ResponseEntity<ErrorInfo> handleAwsS3Exception(AwsS3Exception ase) {
        return createErrorResponse("AWS S3 Exception", HttpStatus.INTERNAL_SERVER_ERROR, ase.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorInfo> handleJwtException(JwtException je) {
        return createErrorResponse("JWT Exception", HttpStatus.UNAUTHORIZED, je.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorInfo> handleAuthenticationException(AuthenticationException ae) {
        return createErrorResponse("Authentication Exception", HttpStatus.FORBIDDEN, ae.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorInfo> handleAuthorizationException(AuthorizationException ae) {
        return createErrorResponse("Authorization Exception", HttpStatus.FORBIDDEN, ae.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorInfo> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException matme) {
        return createErrorResponse("Bad Request Exception", HttpStatus.BAD_REQUEST, matme.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorInfo> handleConstraintViolationException(ConstraintViolationException e) {
        logger.error(e);
        return createErrorResponse("Constraint Violation Exception", HttpStatus.UNPROCESSABLE_ENTITY
                , "The request failed due to constraint violations."
                , e.getConstraintViolations().stream().map(
                    cv -> new FieldError("", ((PathImpl) cv.getPropertyPath()).getLeafNode().toString(), cv.getInvalidValue(), true, null, null, cv.getMessageTemplate())
                ).collect(Collectors.toList()));
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorInfo> handleDataNotFoundException(DatabaseException de) {
        return createErrorResponse("Database Exception", HttpStatus.INTERNAL_SERVER_ERROR, de.getMessage());
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorInfo> handleDataNotFoundException(DataNotFoundException dnf) {
        return createErrorResponse("Data Not Found Exception", HttpStatus.NOT_FOUND, dnf.getMessage());
    }

    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<ErrorInfo> handleDuplicateDataException(DuplicateDataException dde) {
        return createErrorResponse("Duplicate Data Exception", HttpStatus.CONFLICT, dde.getMessage());
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorInfo> handleOperationNotAllowedException(OperationNotAllowedException onae) {
        return createErrorResponse("Operation Not Allowed Exception", HttpStatus.FORBIDDEN, onae.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorInfo> handleValidationException(ValidationException ve) {
        return createErrorResponse("Validation Exception", HttpStatus.UNPROCESSABLE_ENTITY, ve.getMessage(), ve.getErrors());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorInfo> handleResourceAlreadyExistsException(ResourceAlreadyExistsException raee) {
        return createErrorResponse("Resource Already Exists Exception", HttpStatus.CONFLICT, raee.getMessage());
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorInfo> handleInvalidDataException(InvalidDataException ide) {
        return createErrorResponse("Invalid Data Exception", HttpStatus.UNPROCESSABLE_ENTITY, ide.getMessage());
    }

    @ExceptionHandler(DataConversionException.class)
    public ResponseEntity<ErrorInfo> handleDataConversionException(DataConversionException dce) {
        return createErrorResponse("Data Conversion Exception", HttpStatus.UNPROCESSABLE_ENTITY, dce.getMessage());
    }

    @ExceptionHandler(DataIntegrityException.class)
    public ResponseEntity<ErrorInfo> handleDataIntegrityException(DataIntegrityException die) {
        return createErrorResponse("Data Integrity Exception", HttpStatus.CONFLICT, die.getMessage());
    }

    @ExceptionHandler(MissingDataException.class)
    public ResponseEntity<ErrorInfo> handleMissingDataException(MissingDataException mde) {
        return createErrorResponse("Missing Data Exception", HttpStatus.UNPROCESSABLE_ENTITY, mde.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorInfo> handleFileUploadException(FileUploadException fue) {
        return createErrorResponse("File Upload Exception", HttpStatus.BAD_REQUEST, fue.getMessage());
    }

}