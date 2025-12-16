package org.sangyunpark.pointpay.domain.order.repository;

import org.sangyunpark.pointpay.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
