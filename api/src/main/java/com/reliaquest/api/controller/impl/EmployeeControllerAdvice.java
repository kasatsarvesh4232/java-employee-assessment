package com.reliaquest.api.controller.impl;

import com.reliaquest.server.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.ControllerAdvice;
        import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class EmployeeControllerAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<String>> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Rate limit exceeded")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Response.error("Rate limit exceeded. Please try again later."));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error("An unexpected error occurred. Please check parameters or URL format!"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred. Please check parameters or URL format!", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error("Internal server error: " + ex.getMessage()));
    }
}
