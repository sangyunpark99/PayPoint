package org.sangyunpark.pointpay.domain.product.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.sangyunpark.pointpay.common.BaseEntity;

@Entity
@Table(name = "products")
@DynamicUpdate
@NoArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int stock;

    private int price;

    @Builder
    public Product(String name, int stock, int price) {
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getStock() {
        return this.stock;
    }

    public int getPrice() {
        return this.price;
    }
}
