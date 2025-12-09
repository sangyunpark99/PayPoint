package org.sangyunpark.pointpay.domain.point.repository;

import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.repository.projection.PointHistoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {
    @Query(value = """
        SELECT
               SUM(CASE WHEN ph.point_type = 'CHARGE' THEN ph.point_balance ELSE 0 END) AS income,
               SUM(CASE WHEN ph.point_type <> 'CHARGE' THEN ph.point_balance ELSE 0 END) AS expense,
               DATE(ph.created_at) AS historyDate
        FROM point_history ph
        WHERE ph.user_id = :userId
            AND ph.created_at >= :startDate
            AND ph.created_at <= :endDate
        GROUP BY DATE(ph.created_at)
        ORDER BY DATE(ph.created_at)
    """, nativeQuery = true)
    List<PointHistoryProjection> findByDate(long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
    SELECT new org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse$PointHistoryPerMonth(
        ph.pointBalance,
        ph.pointType,
        ph.createdAt
    )
    FROM PointHistory ph
    WHERE ph.user.id = :userId
      AND ph.createdAt >= :startDate
      AND ph.createdAt <= :endDate
""")
    List<PointHistoriesPerMonthResponse.PointHistoryPerMonth>
    findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}