package org.sangyunpark.pointpay.domain.point.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.event.service.PointHistoryEventService;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointChargeResponse;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJdbcRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private static final int MAX_POINT = 2_100_000_000;

    private final UserJpaRepository userJpaRepository;
    private final PointHistoryEventService pointHistoryEventService;
    private final PointHistoryCacheService pointHistoryCacheService;
    private final PointHistoryJdbcRepository pointHistoryJdbcRepository;

    @Transactional
    public PointChargeResponse chargePoint(long userId, int pointAmount) {
        LocalDateTime createdAt = LocalDateTime.now();

        User user = userJpaRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        long balanceBefore = user.getPointBalance();
        user.validatePointCharge(pointAmount, MAX_POINT);
        int updated = userJpaRepository.increasePoint(userId, pointAmount, MAX_POINT);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.CHARGE_POINT_FAILED);
        }

        int currentPoint = userJpaRepository.findPointBalanceById(userId);
        PointHistoryEvent event = PointHistoryEvent.builder()
                .userId(userId)
                .balanceBefore(balanceBefore)
                .balanceAfter(currentPoint)
                .pointType(PointType.CHARGE)
                .createdAt(createdAt)
                .build();

        pointHistoryEventService.savePointHistoryEvent(event);
        pointHistoryCacheService.detailPointHistoryPerTodayEvict(userId);

        return PointChargeResponse.builder()
                .userId(user.getId())
                .chargePoint(pointAmount)
                .currentPoint(currentPoint)
                .build();
    }

    @Transactional(readOnly = true)
    public PointHistoriesSummaryResponse getPointHistoriesSummary(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return pointHistoryCacheService.getSummaryPointHistories(userId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public PointHistoriesResponse getPointHistoryDetailPerMonth(long userId, LocalDateTime startDate) {
        return pointHistoryCacheService.getDetailPointHistoriesPerMonth(userId, startDate);
    }

    @Transactional(readOnly = true)
    public PointHistoriesResponse getPointHistoryDetailPerToday(long userId) {
        return pointHistoryCacheService.getDetailPointHistoriesPerToday(userId);
    }

    @Transactional
    public int savePointHistoryFromEvent(List<PointHistoryEvent> pointHistoryEvents, List<Long> ids) {
        List<PointHistory> pointHistories = new ArrayList<>(pointHistoryEvents.size());

        for(PointHistoryEvent event : pointHistoryEvents) {
            User user = new User(event.getUserId());
            pointHistories.add(PointHistory.fromEvent(event, user));
        }

        pointHistoryJdbcRepository.batchInsert(pointHistories);

        int updated = pointHistoryEventService.updateEventStatusInIds(EventStatus.PROCESSING, EventStatus.FINISH, ids);
        if(updated == 0) {
            throw new BusinessException(ErrorCode.FAILED_CONVERT_EVENT_STATUS);
        }

        return pointHistoryEvents.size();
    }

    public void batchInsert(List<PointHistory> pointHistories) {
        pointHistoryJdbcRepository.batchInsert(pointHistories);
    }
}