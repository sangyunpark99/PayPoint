package org.sangyunpark.pointpay.domain.idempotency;

import org.sangyunpark.pointpay.common.BaseEntity;
import java.time.LocalDateTime;

public class IdempotencyRecord extends BaseEntity {

    private int statusCode;
    private String responseBody;
    private IdempotencyStatus status;

    protected IdempotencyRecord() {}

    private IdempotencyRecord(int statusCode, String responseBody, LocalDateTime createdAt, IdempotencyStatus status) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static IdempotencyRecord inProgress() {
        return new IdempotencyRecord(
                0,
                "",
                LocalDateTime.now(),
                IdempotencyStatus.STARTED
        );
    }

    public static IdempotencyRecord completed(int httpStatus, String body) {
        return new IdempotencyRecord(
                httpStatus,
                body,
                LocalDateTime.now(),
                IdempotencyStatus.SUCCESS
        );
    }

    public static IdempotencyRecord failed(int httpStatus, String body) {
        return new IdempotencyRecord(
                httpStatus,
                body,
                LocalDateTime.now(),
                IdempotencyStatus.FAILED
        );
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public IdempotencyStatus getStatus() {
        return this.status;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
