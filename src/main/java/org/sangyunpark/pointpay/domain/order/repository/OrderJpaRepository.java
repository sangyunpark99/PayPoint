package org.sangyunpark.pointpay.domain.order.repository;

import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
