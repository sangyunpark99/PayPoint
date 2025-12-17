package org.sangyunpark.pointpay.domain.point.repository;

import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.repository.projection.PointHistoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {
    @Query(value = """
        SELECT
               SUM(CASE WHEN ph.point_type = 'CHARGE' THEN ph.point_balance ELSE 0 END) AS income,
               SUM(CASE WHEN ph.point_type <> 'CHARGE' THEN ph.point_balance ELSE 0 END) AS expense,
               ph.history_date AS historyDate
        FROM point_history ph
        WHERE ph.user_id = :userId
            AND ph.history_date >= :startDate
            AND ph.history_date <= :endDate
        GROUP BY ph.history_date
        ORDER BY ph.history_date
    """, nativeQuery = true)
    List<PointHistoryProjection> findByDate(long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT new org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse$PointHistoryPerMonth(
            ph.pointBalance,
            ph.pointType,
            ph.createdAt
        )
        FROM PointHistory ph
        WHERE ph.user.id = :userId
          AND ph.createdAt >= :startDate
          AND ph.createdAt <= :endDate
    """)
    List<PointHistoriesResponse.PointHistoryPerMonth> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
       SELECT new org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse$PointHistoryPerMonth(
            ph.pointBalance,
            ph.pointType,
            ph.createdAt
        )
        FROM PointHistory ph
        WHERE ph.user.id = :userId
            AND ph.createdAt >= :startDate
            AND ph.createdAt <= :endDate
    """)
    List<PointHistoriesResponse.PointHistoryPerMonth> findAllByUserIdAndCreatedAtToday(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}