package org.sangyunpark.pointpay.domain.point.cache;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class PointHistoryPreWarmRunner implements ApplicationRunner {

    private static final LocalDate PREWARM_START = LocalDate.of(2025,10,1);
    private static final LocalDate PREWARM_END = LocalDate.of(2025,11,1);

    private final PointHistoryCacheManager cacheManager;
    private final UserJpaRepository userJpaRepository;

    public PointHistoryPreWarmRunner(PointHistoryCacheManager cacheManager, UserJpaRepository userJpaRepository) {
        this.cacheManager = cacheManager;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocalDateTime startDateTime = PREWARM_START.atStartOfDay();
        LocalDateTime endDateTime = PREWARM_END.atStartOfDay();

        List<Long> userIds = userJpaRepository.findAllIds();

        for (Long userId : userIds) {
            try {
                cacheManager.loadAndCache(userId, startDateTime, endDateTime);
            } catch (Exception e) {
                log.error("캐싱 데이터 적재 실패");
            }
        }
    }
}