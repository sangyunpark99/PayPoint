package org.sangyunpark.pointpay.domain.order.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.order.controller.dto.request.OrderCreateRequest;
import org.sangyunpark.pointpay.domain.order.repository.OrderItemJpaRepository;
import org.sangyunpark.pointpay.domain.order.repository.OrderJpaRepository;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.sangyunpark.pointpay.domain.product.repository.ProductJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrencyOrderServiceTest extends CommonTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("동시에 여러 주문이 들어와도 재고는 0 미만으로 내려가지 않고 초과 판매가 발생하지 않습니다.")
    void whenConcurrentOrders_thenNotOversell() throws InterruptedException {
        // given
        User user = userJpaRepository.save(new User("test", 100000));
        Product product = productJpaRepository.save(new Product("test_상품", 100, 1000));

        int stock = 100;
        int tasks = 200;
        int quantity = 1;

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(tasks);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger outOfStocks = new AtomicInteger(0);
        AtomicInteger otherErrors = new AtomicInteger(0);

        long userId = user.getId();
        long productId = product.getId();

        for (int i = 0; i < tasks; i++) {
            pool.submit(() -> {
                try {
                    start.await();

                    List<OrderCreateRequest.Item> items = List.of(
                            new OrderCreateRequest.Item(productId, quantity)
                    );

                    orderService.createOrder(userId, items);
                    success.incrementAndGet();

                } catch (BusinessException e) {
                    if (e.getErrorCode().equals(ErrorCode.OUT_OF_STOCK)) {
                        outOfStocks.incrementAndGet();
                    } else {
                        otherErrors.incrementAndGet();
                        throw e;
                    }
                } catch (Exception e) {
                    otherErrors.incrementAndGet();
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await();
        pool.shutdown();

        // then
        entityManager.clear();
        Product updated = productJpaRepository.findById(productId).orElseThrow();

        long orderCount = orderJpaRepository.count();
        long orderItemCount = orderItemJpaRepository.count();

        assertThat(otherErrors.get()).isEqualTo(0);          // OUT_OF_STOCK 말고 다른 에러 없어야 함
        assertThat(success.get()).isEqualTo(stock);
        assertThat(outOfStocks.get()).isEqualTo(tasks - stock);
        assertThat(updated.getStock()).isEqualTo(0);
        assertThat(orderCount).isEqualTo(stock);
        assertThat(orderItemCount).isEqualTo(stock);
    }

    @BeforeEach
    void clear() {
        dbCleaner.clear();
    }
}
