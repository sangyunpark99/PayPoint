package org.sangyunpark.pointpay.domain.point.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.ServiceTest;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PointServiceTest extends ServiceTest {

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Test
    @DisplayName("입력한 기간 내의 포인트 내역을 조회합니다.")
    @Transactional
    public void getPointHistoriesTest() {

        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        List<PointHistory> pointHistories = new ArrayList<>(List.of(
                new PointHistory(new User(1L), PointType.CHARGE, 300),
                new PointHistory(new User(1L), PointType.USE, 300),
                new PointHistory(new User(1L), PointType.TRANSFER, 300)
        ));

        pointHistoryJpaRepository.saveAll(pointHistories);

        // when
        List<PointHistory> getPointHistories = pointHistoryJpaRepository.findByDate(1L, startDate, endDate);

        // then
        assertThat(getPointHistories.size()).isEqualTo(3);
    }
}
