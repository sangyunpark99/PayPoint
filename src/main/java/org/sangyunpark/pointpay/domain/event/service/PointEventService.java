package org.sangyunpark.pointpay.domain.event.service;

import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.event.repository.PointHistoryEventRepository;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointEventService {

    private final PointHistoryEventRepository pointHistoryEventRepository;

    public PointEventService(PointHistoryEventRepository pointHistoryEventRepository) {
        this.pointHistoryEventRepository = pointHistoryEventRepository;
    }

    @Transactional
    public void savePointEvent(long userId, int pointBalance, PointType pointType) {
        PointHistoryEvent event = PointHistoryEvent.builder()
                .userId(userId)
                .pointAmount(pointBalance)
                .pointType(pointType).build();
        pointHistoryEventRepository.save(event);
    }

    public List<PointHistoryEvent> getEventStatusIsReady() {
        return pointHistoryEventRepository.findTop10000ByEventStatus(EventStatus.READY);
    }
}