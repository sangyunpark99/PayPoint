package org.sangyunpark.pointpay.domain.event.repository;

import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryEventRepository extends JpaRepository<PointHistoryEvent, Long> {
    List<PointHistoryEvent> findTop10000ByEventStatus(EventStatus status);
}
