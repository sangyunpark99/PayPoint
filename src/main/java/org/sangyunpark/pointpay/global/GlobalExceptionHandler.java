package org.sangyunpark.pointpay.global;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyRecord;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyService;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String IDEMPOTENCY_KEY = "IDEMPOTENCY_KEY";
    private final IdempotencyService idempotencyService;

    @ExceptionHandler(exception = BindException.class)
    public ResponseEntity<?> handleBindException(BindException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(exception = BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e, HttpServletRequest request) {

        Object keyAttr = request.getAttribute(IDEMPOTENCY_KEY);
        if (keyAttr != null) {
            String key = keyAttr.toString();
            IdempotencyRecord record = IdempotencyRecord.failed(e.getErrorCode().getStatus().value(), e.getMessage());
            idempotencyService.failed(key, record);
        }
        return ResponseEntity.status(e.getStatusCode().value()).body(e.getMessage());
    }
}