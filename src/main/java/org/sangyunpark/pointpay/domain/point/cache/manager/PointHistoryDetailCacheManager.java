package org.sangyunpark.pointpay.domain.point.cache.manager;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse.*;

@Slf4j
@Component
public class PointHistoryDetailCacheManager implements CacheManager<PointHistoryDetailCacheKey, PointHistoriesPerMonthResponse>{

    private final Cache<PointHistoryDetailCacheKey, PointHistoriesPerMonthResponse> cache;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistoryDetailCacheManager(
            @Qualifier("pointHistoryDetailCache") Cache<PointHistoryDetailCacheKey, PointHistoriesPerMonthResponse> cache,
            PointHistoryJpaRepository pointHistoryJpaRepository
    )
    {
        this.cache = cache;
        this.pointHistoryJpaRepository = pointHistoryJpaRepository;
    }

    public PointHistoriesPerMonthResponse load(PointHistoryDetailCacheKey key) {
        log.info("cache size = {}", cache.asMap().size());
        return cache.getIfPresent(key);
    }

    public PointHistoriesPerMonthResponse loadAndCache(PointHistoryDetailCacheKey key) {
        return cache.get(key, v -> loadFromDb(key));
    }

    public PointHistoriesPerMonthResponse loadFromDb(PointHistoryDetailCacheKey key) {
        long userId = key.getUserId();
        YearMonth yearMonth = key.getYearMonth();

        LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        List<PointHistory> pointHistories = pointHistoryJpaRepository.findAllByUserIdAndCreatedAtBetween(userId, monthStart, monthEnd);

        List<PointHistoryPerMonth> response = pointHistories.stream().map(PointHistoryPerMonth::from).toList();
        return new PointHistoriesPerMonthResponse(response);
    }
}
