package org.sangyunpark.pointpay.domain.point.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.event.service.PointHistoryEventService;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.service.PointService;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class PointScheduler {

    private static int BATCH_SIZE = 10000;

    private final PointHistoryEventService pointHistoryEventService;
    private final PointService pointService;

    @Scheduled(
        cron = "* * * * * *"
    )
    @Transactional
    public void pointHistoryConsumer() {

        List<PointHistoryEvent> pointHistoryEvents = pointHistoryEventService.findPointHistoryEventByEventStatus(EventStatus.READY, BATCH_SIZE);
        List<Long> ids = pointHistoryEvents.stream().map(PointHistoryEvent::getId).toList();
        pointHistoryEventService.updateEventStatusInIds(EventStatus.READY, EventStatus.PROCESSING, ids);
        List<PointHistory> pointHistories = new ArrayList<>(pointHistoryEvents.size());

        for(PointHistoryEvent event : pointHistoryEvents) {
            User user = new User(event.getUserId());
            pointHistories.add(PointHistory.fromEvent(event, user));
        }

        pointService.batchInsert(pointHistories);
        pointHistoryEventService.updateEventStatusInIds(EventStatus.PROCESSING, EventStatus.FINISH, ids);
    }
}