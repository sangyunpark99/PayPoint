package org.sangyunpark.pointpay.domain.product.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ProductCursorPageResponse {

    private List<ProductResponse> item;

    private boolean hasNext;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime nextCursorCreatedAt;
    private Long nextCursorId;

    public ProductCursorPageResponse(List<ProductResponse> item, boolean hasNext, LocalDateTime nextCursorCreatedAt, Long nextCursorId) {
        this.item = item;
        this.hasNext = hasNext;
        this.nextCursorCreatedAt = nextCursorCreatedAt;
        this.nextCursorId = nextCursorId;
    }
}
