package org.sangyunpark.pointpay.domain.point.service;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.point.dto.response.PointBalanceResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.entity.PointStatus;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryRepository;
import org.sangyunpark.pointpay.error.BusinessException;
import org.sangyunpark.pointpay.error.ErrorCode;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserRepository;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            backoff = @Backoff(delay = 40),
            recover = "recoverChargePoint"
    )
    @Transactional
    public void chargePoint(final long userId, final int pointAmount) { // 포인트 충전
        final User user = findUser(userId);
        user.chargePoint(pointAmount);
        savePointHistory(user, PointType.CHARGE, pointAmount, PointStatus.SUCCESS);
    }

    @Recover
    @Transactional
    public void recoverChargePoint(final CannotAcquireLockException e, final long userId, final int pointAmount) {
        log.error("[Recover] DeadLock 재시도 실패 - userId = {}, pointAmount = {}", userId, pointAmount);
        final User user = new User(userId);
        savePointHistory(user, PointType.CHARGE, pointAmount, PointStatus.FAIL);
    }

    @Transactional
    public void transferPoint(final long fromId, final long toId, final int pointAmount) {
        final User fromUser = findUser(Math.min(fromId, toId));
        final User toUser = findUser(Math.max(fromId, toId));

        fromUser.usePoint(pointAmount);
        toUser.chargePoint(pointAmount);

        savePointHistory(toUser, PointType.CHARGE, pointAmount, PointStatus.SUCCESS);
        savePointHistory(fromUser, PointType.USE, pointAmount, PointStatus.SUCCESS);
    }

    @Transactional(readOnly = true)
    public PointBalanceResponse getPointBalance(final long userId) {
        final User user = findUser(userId);
        return new PointBalanceResponse(userId, user.getPointBalance());
    }

    private void savePointHistory(final User user, final PointType type, final int addPoint, final PointStatus status) {
        PointHistory pointHistory = new PointHistory(user, type, addPoint, status);
        pointHistoryRepository.save(pointHistory);
    }

    private User findUser(final long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
