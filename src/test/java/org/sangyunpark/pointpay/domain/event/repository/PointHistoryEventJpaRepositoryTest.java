package org.sangyunpark.pointpay.domain.event.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.RepositoryTest;
import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

class PointHistoryEventJpaRepositoryTest extends RepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PointHistoryEventJpaRepository pointHistoryEventJpaRepository;

    @Test
    @DisplayName("여러개 포인트 이벤트를 업데이트 할 경우, 업데이트 성공한 갯수 만큼 숫자를 반환합니다.")
    void whenUpdated_thenReturnUpdatedCount() {
        // given
        User user = new User("test", 0);
        User savedUser = userJpaRepository.save(user);
        PointHistoryEvent pointHistoryEvent1 = PointHistoryEvent.builder().userId(savedUser.getId()).balanceBefore(100).balanceAfter(200).pointType(PointType.CHARGE).createdAt(LocalDateTime.now()).build();
        PointHistoryEvent pointHistoryEvent2 = PointHistoryEvent.builder().userId(savedUser.getId()).balanceBefore(100).balanceAfter(200).pointType(PointType.CHARGE).createdAt(LocalDateTime.now()).build();
        PointHistoryEvent pointHistoryEvent3 = PointHistoryEvent.builder().userId(savedUser.getId()).balanceBefore(100).balanceAfter(200).pointType(PointType.CHARGE).createdAt(LocalDateTime.now()).build();

        PointHistoryEvent savedPointHistoryEvent1 = pointHistoryEventJpaRepository.save(pointHistoryEvent1);
        PointHistoryEvent savedPointHistoryEvent2 = pointHistoryEventJpaRepository.save(pointHistoryEvent2);
        PointHistoryEvent savedPointHistoryEvent3 = pointHistoryEventJpaRepository.save(pointHistoryEvent3);

        List<Long> ids = List.of(savedPointHistoryEvent1.getId(), savedPointHistoryEvent2.getId(), savedPointHistoryEvent3.getId());
        int updated = pointHistoryEventJpaRepository.updateStatusInIds(ids, EventStatus.READY, EventStatus.FINISH);

        Assertions.assertThat(updated).isEqualTo(3);
    }

}