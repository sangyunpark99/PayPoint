package org.sangyunpark.pointpay.domain.product.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.sangyunpark.pointpay.domain.product.entity.Product;

@Getter
public class ProductResponse {

    private long productId;
    private String name;
    private int stock;
    private int price;

    @Builder
    public ProductResponse(long productId, String name, int stock, int price) {
        this.productId = productId;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public static ProductResponse from (Product product) {
        return ProductResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();
    }
}
