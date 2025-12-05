package org.sangyunpark.pointpay.domain.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

public class CaffeinePerKeyLoaderTest {

    @Test
    @DisplayName("동일 키에 대해 per-key로딩을 보장")
    void sameKeyConcurrentLoadCallsLoaderOnlyOnceTest() throws Exception {
        Cache<String, String> cache = Caffeine.newBuilder()
                .build();

        String key = "user:1:2025-10";
        AtomicInteger loadCount = new AtomicInteger(0);
        int threadCount = 300;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    return cache.get(key, k -> {
                        int current = loadCount.incrementAndGet();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "value-" + current;
                    });

                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        startLatch.countDown();

        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(loadCount.get()).isEqualTo(1);

        String expected = futures.get(0).get();
        for (Future<String> f : futures) {
            assertThat(f.get()).isEqualTo(expected);
        }
    }
}