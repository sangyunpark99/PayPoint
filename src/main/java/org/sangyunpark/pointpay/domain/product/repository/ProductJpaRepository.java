package org.sangyunpark.pointpay.domain.product.repository;

import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStockIfEnough(long productId, int quantity);

    @Query("""
           SELECT p
           FROM Product p
           WHERE (:cursorCreatedAt IS NULL AND :cursorId IS NULL)
                   OR(p.createdAt < :cursorCreatedAt)
                   OR(p.createdAt = :cursorCreatedAt AND p.id < :cursorId)
           ORDER BY p.createdAt DESC, p.id DESC
        """)
    List<Product> findNextPage(
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            Pageable pageable
    );
}
