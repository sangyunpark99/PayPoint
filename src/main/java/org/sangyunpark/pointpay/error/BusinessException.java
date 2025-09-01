package org.sangyunpark.pointpay.error;

import org.springframework.http.HttpStatusCode;

public class BusinessException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public BusinessException(final ErrorCode errorCode) {
        this.statusCode = errorCode.getStatus();
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
