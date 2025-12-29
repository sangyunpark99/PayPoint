package org.sangyunpark.pointpay.domain.user.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.RepositoryTest;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class UserJpaRepositoryTest extends RepositoryTest {

    private static final int MAX_POINT = 2_100_000_000;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    @Transactional
    @DisplayName("포인트 충전이 성공하는 경우 1을 반환합니다.")
    void whenIncreasePointSuccess_thenReturnOne() {
        User user = new User("test_user", 0);
        User savedUser = userJpaRepository.save(user);
        int updated = userJpaRepository.increasePoint(savedUser.getId(), 1000, MAX_POINT);
        Assertions.assertThat(updated).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("포인트 충전이 실패하는 경우 0을 반환합니다.")
    void whenIncreasePointFailed_thenReturnZero() {
        User user = new User("test_user", 0);
        userJpaRepository.save(user);

        long notSavedUserId = 1L;
        int updated = userJpaRepository.increasePoint(notSavedUserId, 1000, MAX_POINT);
        Assertions.assertThat(updated).isEqualTo(0);
    }

    @Test
    @Transactional
    @DisplayName("포인트 차감이 성공하는 경우 1을 반환합니다.")
    void whenDecreasePointSuccess_thenReturnOne() {
        User user = new User("test_user", 1000);
        User savedUser = userJpaRepository.save(user);
        int updated = userJpaRepository.decreasePoint(savedUser.getId(), 1000);
        Assertions.assertThat(updated).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("포인트 차감이 실패하는 경우 0을 반환합니다.")
    void whenDecreasePointFailed_thenReturnZero() {
        User user = new User("test_user", 1000);
        userJpaRepository.save(user);

        long notSavedUserId = 1L;
        int updated = userJpaRepository.decreasePoint(notSavedUserId, 1000);
        Assertions.assertThat(updated).isEqualTo(0);
    }
}
