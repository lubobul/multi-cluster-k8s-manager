package com.multikube_rest_service.advice;

import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.exceptions.UnauthorizedException;
import com.multikube_rest_service.rest.RestErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RestErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RestErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<RestErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RestErrorResponse> handleUnauthorizedException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new RestErrorResponse(ex.getMessage()));
    }
}
