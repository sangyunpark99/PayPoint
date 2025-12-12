package org.sangyunpark.pointpay.domain.point.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.event.service.PointEventService;
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
@ConditionalOnProperty(
        prefix = "scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class PointScheduler {

    private final PointEventService pointEventService;
    private final PointService pointService;

    public PointScheduler(PointEventService pointEventService, PointService pointService) {
        this.pointEventService = pointEventService;
        this.pointService = pointService;
    }

    @Scheduled(
        cron = "* * * * * *"
    )
    @Transactional
    public void reprocessFailedPointCharge() {

        List<PointHistoryEvent> pointHistoryEvents = pointEventService.getEventStatusIsReady();
        log.info("[PointScheduler] FAIL 상태인 포인트 충전 이력을 재처리 합니다. 대상 건수 = {}", pointHistoryEvents.size());

        ArrayList<PointHistory> pointHistories = new ArrayList<>(pointHistoryEvents.size());
        for(PointHistoryEvent event : pointHistoryEvents) {
            User user = new User(event.getUserId());
            PointHistory history = PointHistory.fromEvent(event, user);
            pointHistories.add(history);
            event.updateEventStatus(EventStatus.FINISH);
        }

        pointService.savePointHistories(pointHistories);
    }
}