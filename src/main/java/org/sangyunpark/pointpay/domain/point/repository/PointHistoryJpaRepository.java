package org.sangyunpark.pointpay.domain.point.repository;

import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {
    @Query("SELECT ph FROM PointHistory ph WHERE ph.user.id = :userId AND ph.createdAt >= :startDate AND ph.createdAt <= :endDate")
    List<PointHistory> findByDate(long userId, LocalDateTime startDate, LocalDateTime endDate);
}
