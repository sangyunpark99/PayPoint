package org.sangyunpark.pointpay.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "중복된 닉네임 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저 입니다."),
    POINT_LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "최대 충전 가능한 포인트를 초과했습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "상품 재고가 부족합니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    CHARGE_POINT_FAILED(HttpStatus.CONFLICT, "포인트 충전에 실패했습니다."),
    PAY_POINT_FAILED(HttpStatus.CONFLICT, "포인트 결제에 실패했습니다."),
    ORDER_STATUS_UPDATE_FAILED_CREATED_PAYING(HttpStatus.INTERNAL_SERVER_ERROR, "주문 상태 업데이트에 실패했습니다.[CREATED] -> [PAYING]"),
    ORDER_STATUS_UPDATE_FAILED_PAYING_PAID(HttpStatus.INTERNAL_SERVER_ERROR, "주문 상태 업데이트에 실패했습니다. [PAYING] -> [PAID]"),
    ALREADY_PROCESSING_EVENT(HttpStatus.INTERNAL_SERVER_ERROR, "이미 처리중인 이벤트입니다."),
    FAILED_CONVERT_EVENT_STATUS(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 상태 업데이트를 실패했습니다.");

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
