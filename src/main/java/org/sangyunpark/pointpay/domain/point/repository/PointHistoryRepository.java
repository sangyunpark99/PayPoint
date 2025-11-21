package org.sangyunpark.pointpay.domain.point.repository;

import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    @Query("SELECT ph.id FROM PointHistory ph WHERE ph.pointStatus = :status")
    List<Long> findIdsByStatus(PointStatus status);

    @Modifying
    @Query("UPDATE PointHistory ph SET ph.pointStatus = :status WHERE ph.id IN :ids")
    void updatePointStatus(List<Long> ids, PointStatus status);

    @Modifying
    @NativeQuery("""
       UPDATE user u
       JOIN (
           SELECT user_id, SUM(point_balance) AS total_add
           FROM point_history
           WHERE id IN (:pointHistoryIds)
           GROUP BY user_Id
       ) AS sums
       ON u.id = sums.user_id
       SET u.point_balance = u.point_balance + sums.total_add;
      """
    )
    void updateUserPoint(List<Long> pointHistoryIds);
}
