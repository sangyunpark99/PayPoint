package org.sangyunpark.pointpay.domain.point.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointStatus;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserRepository;
import org.sangyunpark.pointpay.error.BusinessException;
import org.sangyunpark.pointpay.error.ErrorCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class PointScheduler {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    public PointScheduler(PointHistoryRepository pointHistoryRepository, UserRepository userRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(
        cron = "* * * * * *"
    )
    @Transactional
    public void reprocessFailedPointCharge() {
        List<PointHistory> pointHistories = pointHistoryRepository.findAllByStatus(PointStatus.FAIL);
        log.info("[PointScheduler] FAIL 상태인 포인트 충전 이력을 재처리 합니다. 대상 건수 = {}", pointHistories.size());
        for(PointHistory pointHistory : pointHistories) {
            long userId = pointHistory.getUserId();
            int addPointBalance = pointHistory.getPointBalance();
            User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            user.chargePoint(addPointBalance);
            pointHistory.updateStatus(PointStatus.SUCCESS);
        }
    }
}