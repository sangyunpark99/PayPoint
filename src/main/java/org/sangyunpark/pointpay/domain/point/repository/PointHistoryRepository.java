package org.sangyunpark.pointpay.domain.point.repository;

import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
