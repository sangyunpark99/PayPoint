package org.sangyunpark.pointpay.domain.point.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointStatus;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
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
        List<PointHistory> pointHistories = pointHistoryRepository.findAllByStatus(PointStatus.FAIL);
        log.info("[PointScheduler] FAIL 상태인 포인트 충전 이력을 재처리 합니다. 대상 건수 = {}", pointHistories.size());
        for(PointHistory pointHistory : pointHistories) {
            final User user = pointHistory.getUser();
            int addPointBalance = pointHistory.getPointBalance();
            user.chargePoint(addPointBalance);
            pointHistory.updateStatus(PointStatus.SUCCESS);
        }
    }
}