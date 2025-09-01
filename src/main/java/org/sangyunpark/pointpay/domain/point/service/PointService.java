package org.sangyunpark.pointpay.domain.point.service;

import jakarta.transaction.Transactional;
import org.sangyunpark.pointpay.error.BusinessException;
import org.sangyunpark.pointpay.error.ErrorCode;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private static final int MAX_POINT = 2_100_000_000;

    private final UserRepository userRepository;

    public PointService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void chargePoint(final long userId, final int addPoint) {
        final User user = findUser(userId);
        checkOverMaxPoint(user.getPointBalance(), addPoint);

        // dirty check vs
    }

    private void checkOverMaxPoint(final int userPoint, final int addPoint) {
        if(userPoint + addPoint > MAX_POINT) {
            throw new BusinessException(ErrorCode.POINT_LIMIT_EXCEED);
        }
    }

    private User findUser(final long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
