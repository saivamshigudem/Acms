package com.acms.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        List<ApiError.ApiSubError> subErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ApiError.ApiSubError.builder()
                        .object(fieldError.getObjectName())
                        .field(fieldError.getField())
                        .rejectedValue(fieldError.getRejectedValue())
                        .message(fieldError.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST)
                .message("Validation failed")
                .debugMessage(ex.getLocalizedMessage())
                .subErrors(subErrors)
                .path(getRequestPath(request))
                .build();

        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        log.error("Message not readable: {}", ex.getMessage());
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request",
                ex.getMessage(),
                getRequestPath(request)
        );
        
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        log.error("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND,
                "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                ex.getMessage(),
                getRequestPath(request)
        );
        
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ex.getLocalizedMessage(),
                getRequestPath(request)
        );
        
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), apiError.getStatus(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        log.error("Constraint violation: {}", ex.getMessage());
        
        List<ApiError.ApiSubError> subErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString();
                    return ApiError.ApiSubError.builder()
                            .object(field.contains(".") ? field.substring(0, field.indexOf('.')) : field)
                            .field(field)
                            .rejectedValue(violation.getInvalidValue())
                            .message(violation.getMessage())
                            .build();
                })
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST)
                .message("Validation error")
                .debugMessage(ex.getLocalizedMessage())
                .subErrors(subErrors)
                .path(getRequestPath(request))
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), apiError.getStatus(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        log.error("Method argument type mismatch: {}", ex.getMessage());
        
        String error = ex.getName() + " should be of type " + 
                Objects.requireNonNull(ex.getRequiredType()).getSimpleName();
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "Type mismatch",
                error,
                getRequestPath(request)
        );
        
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), apiError.getStatus(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Database error";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            message = ex.getCause().getMessage();
        }
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT,
                "Data integrity violation",
                message,
                getRequestPath(request)
        );
        
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), apiError.getStatus(), request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                ex.getLocalizedMessage(),
                getRequestPath(request)
        );
        
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), apiError.getStatus(), request);
    }

    private String getRequestPath(WebRequest request) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getRequestURI();
        }
        return "";
    }
}
