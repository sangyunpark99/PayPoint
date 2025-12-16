package org.sangyunpark.pointpay.domain.order.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.sangyunpark.pointpay.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderDetailResponse {

    private final long orderId;
    private final OrderStatus status;
    private final long totalAmount;
    private final String paymentTxId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private final List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private final long orderItemId;
        private final long productId;
        private final String productName;

        private final long unitPrice;   // 주문 시점 가격 스냅샷
        private final long quantity;
        private final long lineAmount;  // unitPrice * quantity
    }

    public static OrderDetailResponse from(Order order) {
        List<Item> items = order.getItems().stream()
                .map(oi -> Item.builder()
                        .orderItemId(oi.getId())
                        .productId(oi.getProduct().getId())
                        .productName(oi.getProduct().getName())
                        .unitPrice(oi.getUnitPrice())
                        .quantity(oi.getQuantity())
                        .lineAmount(oi.getUnitPrice() * oi.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .paymentTxId(order.getPaymentTxId()) // 없으면 null
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }
}
