package org.sangyunpark.pointpay.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "중복된 닉네임 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저 입니다."),
    POINT_LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "최대 충전 가능한 포인트를 초과했습니다.");

    private final HttpStatus status;
    private final String description;

    ErrorCode(final HttpStatus status, final String description) {
        this.status = status;
        this.description = description;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public String getDescription() {
        return this.description;
    }
}
