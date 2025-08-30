package org.sangyunpark.pointpay.point.repository;

import org.sangyunpark.pointpay.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<PointHistory, Long> {
}
