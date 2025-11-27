package org.sangyunpark.pointpay.domain.point.service;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.event.service.PointEventService;
import org.sangyunpark.pointpay.domain.point.dto.response.PointBalanceResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJdbcRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PointService {

    private final UserService userService;
    private final PointHistoryJdbcRepository pointHistoryJdbcRepository;
    private final PointEventService pointEventService;

    public PointService(UserService userService, PointEventService pointEventService, PointHistoryJdbcRepository pointHistoryJdbcRepository) {
        this.userService = userService;
        this.pointEventService = pointEventService;
        this.pointHistoryJdbcRepository = pointHistoryJdbcRepository;
    }

    @Transactional
    public void chargePoint(final long userId, final int pointAmount) {
        User user = userService.findUserById(userId);
        user.chargePoint(pointAmount);
        pointEventService.savePointEvent(userId, pointAmount, PointType.CHARGE);
    }

    @Transactional
    public void savePointHistories(List<PointHistory> pointHistories) {
        pointHistoryJdbcRepository.batchInsert(pointHistories);
    }

    @Transactional(readOnly = true)
    public PointBalanceResponse getPointBalance(final long userId) {
        final User user = userService.findUserById(userId);
        return new PointBalanceResponse(userId, user.getPointBalance());
    }
}