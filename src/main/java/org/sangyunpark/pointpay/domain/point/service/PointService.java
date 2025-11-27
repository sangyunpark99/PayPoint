package org.sangyunpark.pointpay.domain.point.service;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.event.service.PointEventService;
import org.sangyunpark.pointpay.domain.point.dto.response.PointBalanceResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJdbcRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.error.BusinessException;
import org.sangyunpark.pointpay.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PointService {

    private static final int MAX_POINT = 2_100_000_000;

    private final UserJpaRepository userJpaRepository;
    private final PointHistoryJdbcRepository pointHistoryJdbcRepository;
    private final PointEventService pointEventService;

    public PointService(UserJpaRepository userJpaRepository, PointEventService pointEventService, PointHistoryJdbcRepository pointHistoryJdbcRepository) {
        this.userJpaRepository = userJpaRepository;
        this.pointEventService = pointEventService;
        this.pointHistoryJdbcRepository = pointHistoryJdbcRepository;
    }

    @Transactional
    public void chargePoint(long userId, int pointAmount) {
        boolean isExisted = userJpaRepository.existsById(userId);
        if(!isExisted) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userJpaRepository.increasePoint(userId, pointAmount, MAX_POINT);
        pointEventService.savePointEvent(userId, pointAmount, PointType.CHARGE);
    }

    @Transactional
    public void savePointHistories(List<PointHistory> pointHistories) {
        pointHistoryJdbcRepository.batchInsert(pointHistories);
    }

    @Transactional(readOnly = true)
    public PointBalanceResponse getPointBalance(long userId) {
        User user = userJpaRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new PointBalanceResponse(userId, user.getPointBalance());
    }
}