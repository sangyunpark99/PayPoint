package org.sangyunpark.pointpay.domain.point.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.event.service.PointEventService;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private static final int MAX_POINT = 2_100_000_000;

    private final UserJpaRepository userJpaRepository;
    private final PointHistoryJdbcRepository pointHistoryJdbcRepository;
    private final PointEventService pointEventService;
    private final PointHistoryCacheService pointHistoryCacheService;

    @Transactional
    public PointChargeResponse chargePoint(long userId, int pointAmount) {
        LocalDateTime createdAt = LocalDateTime.now();

        User user = userJpaRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.validatePointCharge(pointAmount, MAX_POINT);
        userJpaRepository.increasePoint(userId, pointAmount, MAX_POINT);

        pointEventService.savePointEvent(userId, pointAmount, PointType.CHARGE, createdAt);
        pointHistoryCacheService.detailPointHistoryPerTodayEvict(userId);

        int currentPoint = userJpaRepository.findPointBalanceById(userId);

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

    @Transactional
    public void savePointHistories(List<PointHistory> pointHistories) {
        pointHistoryJdbcRepository.batchInsert(pointHistories);
    }

    @Transactional(readOnly = true)
    public PointHistoriesResponse getPointHistoryDetailPerMonth(long userId, LocalDateTime startDate) {
       return pointHistoryCacheService.getDetailPointHistoriesPerMonth(userId, startDate);
    }

    @Transactional(readOnly = true)
    public PointHistoriesResponse getPointHistoryDetailPerToday(long userId) {
        return pointHistoryCacheService.getDetailPointHistoriesPerToday(userId);
    }
}