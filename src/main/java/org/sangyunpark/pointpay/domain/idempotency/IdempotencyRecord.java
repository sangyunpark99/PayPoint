package org.sangyunpark.pointpay.domain.idempotency;

import org.sangyunpark.pointpay.common.BaseEntity;
import java.time.LocalDateTime;

public class IdempotencyRecord extends BaseEntity {

    private int status;
    private String responseBody;

    protected IdempotencyRecord() {}

    public IdempotencyRecord(int status, String responseBody, LocalDateTime createdAt) {
        this.status = status;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
    }

    public int getStatus() {
        return this.status;
    }

    public String getResponseBody() {
        return this.responseBody;
    }
}
