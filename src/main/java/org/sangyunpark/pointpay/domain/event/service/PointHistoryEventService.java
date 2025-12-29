package org.sangyunpark.pointpay.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.event.repository.PointHistoryEventJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryEventService {

    private final PointHistoryEventJpaRepository pointHistoryEventJpaRepository;

    @Transactional
    public int updateEventStatusInIds(EventStatus from, EventStatus to, List<Long> ids) {
        int updated = pointHistoryEventJpaRepository.updateStatusInIds(ids, from, to);
        if(updated == 0) {
            throw new BusinessException(ErrorCode.FAILED_CONVERT_EVENT_STATUS);
        }

        return updated;
    }

    @Transactional(readOnly = true)
    public List<PointHistoryEvent> findPointHistoryEventByEventStatus(EventStatus status, int limitSize) {
        return  pointHistoryEventJpaRepository.findByEventStatus(status, PageRequest.of(0,limitSize));
    }

    @Transactional
    public PointHistoryEvent savePointHistoryEvent(PointHistoryEvent event) {
        return pointHistoryEventJpaRepository.save(event);
    }
}
