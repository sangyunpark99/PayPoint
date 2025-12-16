package org.sangyunpark.pointpay.exception;

import org.springframework.http.HttpStatusCode;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatusCode statusCode;
    private final String errorMessage;

    public BusinessException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.statusCode = errorCode.getStatus();
        this.errorMessage = errorCode.getDescription();
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
