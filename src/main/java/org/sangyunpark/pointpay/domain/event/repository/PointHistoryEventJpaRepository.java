package org.sangyunpark.pointpay.domain.event.repository;

import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryEventJpaRepository extends JpaRepository<PointHistoryEvent, Long> {

    @Query("SELECT phe FROM PointHistoryEvent phe WHERE phe.eventStatus = :status")
    List<PointHistoryEvent> findByEventStatus(EventStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE PointHistoryEvent phe set phe.eventStatus = :to WHERE phe.eventStatus = :from and phe.id in (:ids)")
    int updateStatusInIds(List<Long> ids, EventStatus from, EventStatus to);

    boolean existsByEventStatus(EventStatus status);
}