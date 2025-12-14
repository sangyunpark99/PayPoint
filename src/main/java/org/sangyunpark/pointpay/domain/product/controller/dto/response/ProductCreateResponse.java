package org.sangyunpark.pointpay.domain.product.controller.dto.response;

import lombok.Builder;
import org.sangyunpark.pointpay.domain.product.entity.Product;

public class ProductCreateResponse {

    private long productId;
    private String name;
    private int  price;
    private int stock;

    @Builder
    public ProductCreateResponse(long productId, String name, int price, int stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public static ProductCreateResponse from (Product product) {
        return ProductCreateResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();

    }
}
