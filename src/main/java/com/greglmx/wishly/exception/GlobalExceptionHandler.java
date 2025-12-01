package com.greglmx.wishly.exception;

import com.greglmx.wishly.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream().map(fe ->
                new ApiError.FieldValidationError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage(), fe.getCode())
        ).collect(Collectors.toList());

        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.BAD_REQUEST.value());
        apiError.setError("Validation Failed");
        apiError.setMessage("One or more fields have invalid values");
        apiError.setPath(request.getRequestURI());
        apiError.setErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiError.FieldValidationError> fieldErrors = ex.getConstraintViolations().stream().map(cv ->
                new ApiError.FieldValidationError(cv.getPropertyPath().toString(), cv.getInvalidValue(), cv.getMessage(), cv.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
        ).collect(Collectors.toList());

        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.BAD_REQUEST.value());
        apiError.setError("Constraint Violation");
        apiError.setMessage("Input did not satisfy constraints");
        apiError.setPath(request.getRequestURI());
        apiError.setErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.UNAUTHORIZED.value());
        apiError.setError("Authentication Failed");
        apiError.setMessage("Invalid username or password");
        apiError.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.CONFLICT.value());
        apiError.setError("Data Integrity Violation");
        apiError.setMessage(ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage());
        apiError.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        apiError.setError("Internal Server Error");
        apiError.setMessage(ex.getMessage());
        apiError.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExists(AlreadyExistsException ex, HttpServletRequest request) {
        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.CONFLICT.value());
        apiError.setError("Already Exists");
        apiError.setMessage(ex.getMessage());
        apiError.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }
}
