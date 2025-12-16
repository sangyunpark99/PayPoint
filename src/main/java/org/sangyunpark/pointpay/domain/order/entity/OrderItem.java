package org.sangyunpark.pointpay.domain.order.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.sangyunpark.pointpay.common.BaseEntity;
import org.sangyunpark.pointpay.domain.product.entity.Product;

@Entity
@Getter
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private long unitPrice;

    private long quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    protected OrderItem() {

    }

    @Builder
    public OrderItem(Product product, long unitPrice, long quantity) {
        this.product = product;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static OrderItem fromProduct(Product product, int quantity) {
        return OrderItem.builder()
                .product(product)
                .unitPrice(product.getPrice())
                .quantity(quantity)
                .build();
    }

    protected void setOrder(Order order) {
        this.order = order;
    }

    public long getUnitPrice() {
        return this.unitPrice;
    }
}
