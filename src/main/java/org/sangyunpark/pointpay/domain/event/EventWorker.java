package org.sangyunpark.pointpay.domain.event;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.event.service.PointHistoryEventService;
import org.sangyunpark.pointpay.domain.point.service.PointService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventWorker {

    private static int BATCH_SIZE = 100;

    private final PointService pointService;
    private final PointHistoryEventService pointHistoryEventService;

    public int start() {
        List<PointHistoryEvent> pointHistoryEvents = pointHistoryEventService.findPointHistoryEventByEventStatus(EventStatus.READY, BATCH_SIZE);
        List<Long> ids = pointHistoryEvents.stream().map(PointHistoryEvent::getId).toList();

        try {
            pointHistoryEventService.updateEventStatusInIds(EventStatus.READY, EventStatus.PROCESSING, ids);
            return pointService.savePointHistoryFromEvent(pointHistoryEvents, ids);
        } catch(Exception e) {
            pointHistoryEventService.updateEventStatusInIds(EventStatus.PROCESSING, EventStatus.READY, ids);
            throw e;
        }
    }
}
