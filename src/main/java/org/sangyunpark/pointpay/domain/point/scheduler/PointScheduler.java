package org.sangyunpark.pointpay.domain.point.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.point.entity.PointStatus;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class PointScheduler {

    private final PointHistoryRepository pointHistoryRepository;

    public PointScheduler(PointHistoryRepository pointHistoryRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @Scheduled(
        cron = "* * * * * *"
    )
    @Transactional
    public void reprocessFailedPointCharge() {
        List<Long> pointHistoryIds = pointHistoryRepository.findIdsByStatus(PointStatus.FAIL);
        log.info("[PointScheduler] FAIL 상태인 포인트 충전 이력을 재처리 합니다. 대상 건수 = {}", pointHistoryIds.size());
        if(!pointHistoryIds.isEmpty()) {
            pointHistoryRepository.updateUserPoint(pointHistoryIds);
            pointHistoryRepository.updatePointStatus(pointHistoryIds, PointStatus.SUCCESS);
        }
    }
}