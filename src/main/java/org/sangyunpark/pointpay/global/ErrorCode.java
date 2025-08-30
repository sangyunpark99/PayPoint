package org.sangyunpark.pointpay.global;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    DUPLICATED_NICKNAME(HttpStatus.CONFLICT);

    private final HttpStatus status;

    ErrorCode(final HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
}
