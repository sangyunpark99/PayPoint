package org.sangyunpark.pointpay.domain.product.controller.dto.request;

public class ProductCreateRequest {

    private String name;
    private int stock;

    public String getName() {
        return this.name;
    }

    public int getStock() {
        return this.stock;
    }
}
