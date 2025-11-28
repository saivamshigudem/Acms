package com.acms.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class ResponseBuilder {
    private ResponseBuilder() {
        // Private constructor to prevent instantiation
    }

    public static <T> ResponseEntity<ApiResponse<T>> buildSuccessResponse(T data, String message, HttpStatus status) {
        String path = getCurrentRequestPath();
        return new ResponseEntity<>(
                ApiResponse.success(data, message, path),
                status
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(String message, HttpStatus status) {
        String path = getCurrentRequestPath();
        return new ResponseEntity<>(
                ApiResponse.error(message, path),
                status
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> buildValidationErrorResponse(String message, T errors, HttpStatus status) {
        String path = getCurrentRequestPath();
        return new ResponseEntity<>(
                ApiResponse.error(message, errors, path),
                status
        );
    }

    private static String getCurrentRequestPath() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            return request.getRequestURI();
        } catch (Exception e) {
            return "";
        }
    }
}
