package org.sangyunpark.pointpay.domain.order.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public class OrderCreateRequest {

    private long userId;

    @Size(min = 1, max = 10)
    private List<Item> items;

    protected OrderCreateRequest() {}

    public static class Item {

        private long productId;

        @Min(1)
        private int quantity;

        protected Item() {}

        public Item(long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public long getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public long getUserId() {
        return userId;
    }

    public List<Item> getItems() {
        return items;
    }
}
