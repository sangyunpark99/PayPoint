package org.sangyunpark.pointpay.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = BusinessException.class)
    public ResponseEntity<?> handleBusinessException(final BusinessException e) {
        return ResponseEntity.status(e.getStatusCode().value()).build();
    }

    @ExceptionHandler(exception = BindException.class)
    public ResponseEntity<?> handleBindException(final BindException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
