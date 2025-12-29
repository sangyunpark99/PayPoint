package org.sangyunpark.pointpay.domain.order.repository;

import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.sangyunpark.pointpay.domain.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserId(long id, long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = :afStatus WHERE o.id = :orderId AND o.status = :bfStatus")
    int updateOrderStatus(long orderId, OrderStatus bfStatus, OrderStatus afStatus);
}
