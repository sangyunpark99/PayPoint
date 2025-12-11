package org.sangyunpark.pointpay.domain.point.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.ServiceTest;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse.PointHistoryPerMonth;
import static org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse.PointHistorySummaryResponse;

public class PointServiceTest extends ServiceTest {

    private static final int MAX_POINT = 2_100_000_000;

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PointService pointService;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @AfterEach
    void cleanUp() {
        try(RedisConnection con = redisConnectionFactory.getConnection()) {
            con.serverCommands().flushDb();
        }
    }

    @Test
    @Transactional
    @DisplayName("입력한 기간 내의 포인트 내역을 조회합니다.")
    public void getPointHistoriesTest() {

        // given
        User user = new User("test", 0);
        User savedUser = userJpaRepository.save(user);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        List<PointHistory> pointHistories = new ArrayList<>(List.of(
                new PointHistory(savedUser, PointType.CHARGE, 300),
                new PointHistory(savedUser, PointType.USE, 300),
                new PointHistory(savedUser, PointType.TRANSFER, 300)
        ));

        pointHistoryJpaRepository.saveAll(pointHistories);

        // when
        List<PointHistorySummaryResponse> response = pointService.getPointHistoriesSummary(user.getId(), startDate, endDate).getPointHistories();
        System.out.println(response);
        // then
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0).getIncome()).isEqualTo(300);
        assertThat(response.get(0).getExpense()).isEqualTo(600);
        assertThat(response.get(0).getDate()).isEqualTo(startDate.toLocalDate());
    }

    @Test
    @Transactional
    @DisplayName("한달 포인트 내역을 조회합니다.")
    public void getPointHistoriesDetailPerMonthTest() {

        // given
        User user = new User("test", 0);
        User savedUser = userJpaRepository.save(user);
        LocalDateTime startDate = LocalDateTime.now();
        List<PointHistory> pointHistories = new ArrayList<>(List.of(
                new PointHistory(savedUser, PointType.CHARGE, 300),
                new PointHistory(savedUser, PointType.USE, 300),
                new PointHistory(savedUser, PointType.TRANSFER, 300)
        ));

        pointHistoryJpaRepository.saveAll(pointHistories);

        // when
        List<PointHistoryPerMonth> result = pointService.getPointHistoryDetailPerMonth(user.getId(), startDate).getPointHistories();
        // then
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0).getPointBalance()).isEqualTo(300);
        assertThat(result.get(0).getPointType()).isEqualTo(PointType.CHARGE);
        assertThat(result.get(1).getPointBalance()).isEqualTo(300);
        assertThat(result.get(1).getPointType()).isEqualTo(PointType.USE);
        assertThat(result.get(2).getPointBalance()).isEqualTo(300);
        assertThat(result.get(2).getPointType()).isEqualTo(PointType.TRANSFER);
    }

    @Test
    @Transactional
    @DisplayName("포인트 충전시 한도 금액을 초과하면 충전되지 않습니다.")
    public void chargePointOverMaxBalance() {
        User user = new User("test", MAX_POINT);
        User savedUser = userJpaRepository.save(user);

        assertThatThrownBy(() -> pointService.chargePoint(savedUser.getId(), 1))
                .isInstanceOf(BusinessException.class)
                .extracting("statusCode")
                .isEqualTo(ErrorCode.POINT_LIMIT_EXCEED.getStatus());
    }
}