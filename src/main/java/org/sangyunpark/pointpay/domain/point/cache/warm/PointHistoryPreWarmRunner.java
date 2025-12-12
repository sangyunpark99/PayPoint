package org.sangyunpark.pointpay.domain.point.cache.warm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailTodayCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.redis.RedisPointHistoryDetailCache;
import org.sangyunpark.pointpay.domain.point.cache.redis.RedisPointHistoryDetailTodayCache;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointHistoryPreWarmRunner implements ApplicationRunner {

    private static final LocalDate PREWARM_START = LocalDate.of(2025,12,1);

    @Value("${cache.warmup.enabled:false}")
    private boolean enabled;

    private final RedisPointHistoryDetailCache redisPointHistoryDetailCache;
    private final RedisPointHistoryDetailTodayCache redisPointHistoryDetailTodayCache;
    private final UserJpaRepository userJpaRepository;

    @Override
    public void run(ApplicationArguments args) {

        if(!enabled) {
            log.info("Cache WarmUp disabled");
            return;
        }

        log.info("Cache WarmUp 시작");
        long startTime = System.currentTimeMillis();

        LocalDateTime startDateTime = PREWARM_START.atStartOfDay();
        List<Long> userIds = userJpaRepository.findAllIds();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        int target = 3000;
        for(int i = 0; i < target; i++) {
            Long userId = userIds.get(i);
            executor.submit(() -> {
                try {
                    redisPointHistoryDetailCache.loadAndCache(
                            new PointHistoryDetailCacheKey(userId, startDateTime)
                    );

                    redisPointHistoryDetailTodayCache.loadAndCache(
                            new PointHistoryDetailTodayCacheKey(userId)
                    );
                } catch (Exception e) {
                    log.error("캐싱 데이터 적재 실패 - userId={}", userId, e);
                }
            });
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                log.warn("Cache WarmUp 종료 시간 초과 - 일부 작업이 끝나지 않았을 수 있습니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Cache WarmUp 대기 중 인터럽트 발생", e);
        }

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;

        log.info("Cache WarmUp 완료 - 대상 유저: {}, 소요 시간: {}s", 5000, seconds);
    }
}