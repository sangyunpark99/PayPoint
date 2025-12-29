package org.sangyunpark.pointpay.domain.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.sangyunpark.pointpay.domain.order.entity.OrderStatus;
import org.sangyunpark.pointpay.domain.order.repository.OrderJpaRepository;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class PaymentServiceTest extends CommonTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderJpaRepository orderJpaRepository; // 마지막 업데이트 실패 시뮬레이션용

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long userId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        dbCleaner.clear();

        User user = new User("test_user", 10_000);
        user = userJpaRepository.save(user);
        userId = user.getId();

        Order order = Order.builder()
                .user(user)
                .totalAmount(3_000L)
                .status(OrderStatus.CREATED)
                .build();
        order = orderJpaRepository.save(order);
        orderId = order.getId();
    }

    @Test
    void orderPayment_success() {
        // when
        paymentService.orderPayment(userId, orderId);

        // then
        Order reloadedOrder = orderJpaRepository.findById(orderId).orElseThrow();
        assertThat(reloadedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        // then
        User reloadedUser = userJpaRepository.findById(userId).orElseThrow();
        assertThat(reloadedUser.getPointBalance()).isEqualTo(7_000L);

        // then
        await().atMost(2, SECONDS).untilAsserted(() -> {
            List<PointHistory> histories = pointHistoryJpaRepository.findAllByUserId(userId);
            assertThat(histories).hasSize(1);
            PointHistory h = histories.get(0);
            assertThat(h.getBalanceBefore()).isEqualTo(10_000L);
            assertThat(h.getBalanceAfter()).isEqualTo(7_000L);
        });
    }

    @Test
    @DisplayName("사용자가 갖고 있는 포인트가 충분하지 않은 경우, 전부 롤백됩니다.")
    void whenPointNotEnough_thenAllRollback() {

        // given
        transactionTemplate.executeWithoutResult(status -> {
            User user = userJpaRepository.findById(userId).orElseThrow();
            userJpaRepository.decreasePoint(user.getId(), 9_000);
        });

        // when
        assertThatThrownBy(() -> paymentService.orderPayment(userId, orderId))
                .isInstanceOf(BusinessException.class);

        // then
        Order reloadedOrder = orderJpaRepository.findById(orderId).orElseThrow();
        assertThat(reloadedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);

        // then
        User reloadedUser = userJpaRepository.findById(userId).orElseThrow();
        assertThat(reloadedUser.getPointBalance()).isEqualTo(1_000);

        // then
        assertThat(pointHistoryJpaRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("주문 상태가 CREATED가 아닌 경우, 주문 결제시 롤백됩니다.")
    void whenFinalStatusUpdateFailed_thenRollback() {
        // given
        orderJpaRepository.updateOrderStatus(orderId, OrderStatus.CREATED, OrderStatus.PAID);

        // when
        assertThatThrownBy(() -> paymentService.orderPayment(userId, orderId))
                .isInstanceOf(BusinessException.class);

        // then
        assertThat(userJpaRepository.findById(userId).orElseThrow().getPointBalance()).isEqualTo(10_000L);
        assertThat(pointHistoryJpaRepository.findAll()).isEmpty();
        assertThat(orderJpaRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("단건 결제 시 PointHistory의 before/after가 실제 잔액 변화와 일치한다")
    void whenPayOnce_thenPointHistoryBeforeAfterIsCorrect() {
        // given
        User user = userJpaRepository.save(new User("u1", 10_000));
        Order order = orderJpaRepository.save(
                Order.builder()
                        .user(user)
                        .totalAmount(3_000L)
                        .status(OrderStatus.CREATED)
                        .build()
        );

        // when
        paymentService.orderPayment(user.getId(), order.getId());

        // then
        await().atMost(2, SECONDS).untilAsserted(() -> {
            User reloaded = userJpaRepository.findById(user.getId()).orElseThrow();
            assertThat(reloaded.getPointBalance()).isEqualTo(7_000L);

            List<PointHistory> histories = pointHistoryJpaRepository.findAll();

            PointHistory h = histories.get(0);
            assertThat(h.getBalanceBefore()).isEqualTo(10_000L);
            assertThat(h.getBalanceAfter()).isEqualTo(7_000L);

            Order paid = orderJpaRepository.findById(order.getId()).orElseThrow();
            assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
        });
    }
}