package org.sangyunpark.pointpay.domain.product.repository;

import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStockIfEnough(long productId, int quantity);
}
