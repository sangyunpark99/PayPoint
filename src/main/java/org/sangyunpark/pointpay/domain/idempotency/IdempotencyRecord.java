package org.sangyunpark.pointpay.domain.idempotency;

import org.sangyunpark.pointpay.common.BaseEntity;
import java.time.LocalDateTime;

public class IdempotencyRecord extends BaseEntity {

    public static final int IN_PROGRESS = 0;
    public static final int COMPLETED = 1;

    private int statusCode;
    private String responseBody;
    private int status;

    protected IdempotencyRecord() {}

    private IdempotencyRecord(int statusCode, String responseBody, LocalDateTime createdAt, int status) {
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
                IN_PROGRESS
        );
    }

    public static IdempotencyRecord completed(int httpStatus, String body) {
        return new IdempotencyRecord(
                httpStatus,
                body,
                LocalDateTime.now(),
                COMPLETED
        );
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public int getStatus() {
        return this.status;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
