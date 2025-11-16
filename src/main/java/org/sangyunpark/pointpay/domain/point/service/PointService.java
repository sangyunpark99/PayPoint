package org.sangyunpark.pointpay.domain.point.service;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.point.dto.response.PointBalanceResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointStatus;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryRepository;
import org.sangyunpark.pointpay.error.BusinessException;
import org.sangyunpark.pointpay.error.ErrorCode;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserRepository;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;

@Slf4j
@Service
public class PointService {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointService(final UserRepository userRepository, final PointHistoryRepository pointHistoryRepository) {
        this.userRepository = userRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }


    @Retryable(
            retryFor = CannotAcquireLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 40)
    )
    @Transactional
    public void chargePoint(final long userId, final int pointAmount) { // 포인트 충전
        final User user = findUser(userId);
        user.chargePoint(pointAmount);
        savePointHistory(user, PointStatus.CHARGE, pointAmount);
    }

    @Transactional
    public void transferPoint(final long fromId, final long toId, final int pointAmount) {
        final User fromUser = findUser(Math.min(fromId, toId));
        final User toUser = findUser(Math.max(fromId, toId));

        fromUser.usePoint(pointAmount);
        toUser.chargePoint(pointAmount);

        savePointHistory(toUser, PointStatus.CHARGE, pointAmount);
        savePointHistory(fromUser, PointStatus.USE, pointAmount);
    }

    @Transactional(readOnly = true)
    public PointBalanceResponse getPointBalance(final long userId) {
        final User user = findUser(userId);
        return new PointBalanceResponse(userId, user.getPointBalance());
    }

    private void savePointHistory(final User user, final PointStatus status, final int addPoint) {
        PointHistory pointHistory = new PointHistory(user, status, addPoint);
        pointHistoryRepository.save(pointHistory);
    }

    private User findUser(final long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
