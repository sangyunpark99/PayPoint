package org.sangyunpark.pointpay.domain.point.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ConcurrencyPointServiceTest extends CommonTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    @DisplayName("동일 회원에 대한 포인트 충전이 동시에 요청될 경우 발생하는 Lost Update 문제 방지합니다.")
    public void concurrentPointChargeTest() throws InterruptedException {
        User user = userJpaRepository.save(new User("test", 0));

        int threadCount = 10;
        int amount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    pointService.chargePoint(user.getId(), amount);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // when
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        User reloaded = userJpaRepository.findById(user.getId())
                .orElseThrow();

        int balance = reloaded.getPointBalance();
        int expected = threadCount * amount;

        assertThat(balance).isEqualTo(expected);
    }
}