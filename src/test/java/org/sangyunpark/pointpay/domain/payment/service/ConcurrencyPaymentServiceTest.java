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
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ConcurrencyPaymentServiceTest extends CommonTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

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
    @DisplayName("동일 주문 동시 요청 20건 -> 포인트 차감 및 원장 저장은 1회만 반영된다")
    void whenConcurrentSameOrder_thenProcessedExactlyOnce() throws Exception {
        //given
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    paymentService.orderPayment(userId, orderId);
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        // then
        Order order = orderJpaRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        User user = userJpaRepository.findById(userId).orElseThrow();
        assertThat(user.getPointBalance()).isEqualTo(10_000L - order.getTotalAmount());

        assertThat(errors)
                .allSatisfy(t -> {
                    assertThat(t).isInstanceOf(BusinessException.class);
                    BusinessException be = (BusinessException) t;
                    assertThat(be.getErrorCode())
                            .isEqualTo(ErrorCode.ORDER_STATUS_UPDATE_FAILED_CREATED_PAYING);
                });

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(errors).hasSize(threads - 1);
    }

    @Test
    @DisplayName("같은 유저가 주문 2개를 동시에 결제하면 PointHistory before/after가 연속적으로 기록되어야 한다")
    void whenPayTwoOrdersConcurrently_thenPointHistoryShouldBeSequential() throws Exception {
        // given
        User user = userJpaRepository.save(new User("u1", 10_000));

        Order order1 = orderJpaRepository.save(
                Order.builder().user(user).totalAmount(3_000L).status(OrderStatus.CREATED).build()
        );
        Order order2 = orderJpaRepository.save(
                Order.builder().user(user).totalAmount(3_000L).status(OrderStatus.CREATED).build()
        );

        int threads = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        // when
        pool.submit(() -> {
            ready.countDown();
            awaitThread(start);
            paymentService.orderPayment(user.getId(), order1.getId());
            done.countDown();
        });

        pool.submit(() -> {
            ready.countDown();
            awaitThread(start);
            paymentService.orderPayment(user.getId(), order2.getId());
            done.countDown();
        });

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        // then
        User reloaded = userJpaRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getPointBalance()).isEqualTo(4_000L);

        assertThat(orderJpaRepository.findById(order1.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PAID);
        assertThat(orderJpaRepository.findById(order2.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PAID);

        await().atMost(3, SECONDS).untilAsserted(() -> {
            List<PointHistory> histories = pointHistoryJpaRepository.findAllByUserId(user.getId());
            assertThat(histories).hasSize(2);

            List<long[]> pairs = histories.stream()
                    .map(h -> new long[]{h.getBalanceBefore(), h.getBalanceAfter()})
                    .toList();

            assertThat(pairs).anySatisfy(p -> {
                assertThat(p[0]).isEqualTo(10_000L);
                assertThat(p[1]).isEqualTo(7_000L);
            });
            assertThat(pairs).anySatisfy(p -> {
                assertThat(p[0]).isEqualTo(7_000L);
                assertThat(p[1]).isEqualTo(4_000L);
            });

            assertThat(histories.stream().map(PointHistory::getBalanceBefore).distinct().count())
                    .isEqualTo(2);
        });
    }

    private static void awaitThread(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
