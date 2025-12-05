package org.sangyunpark.pointpay.domain.point.cache.manager;

import com.github.benmanes.caffeine.cache.Cache;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistorySummaryCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.point.repository.projection.PointHistoryProjection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse.PointHistorySummaryResponse;

@Component
public class PointHistorySummaryCacheManager implements CacheManager<PointHistorySummaryCacheKey, PointHistoriesSummaryResponse>{

    private final Cache<PointHistorySummaryCacheKey, PointHistoriesSummaryResponse> cache;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistorySummaryCacheManager(
            @Qualifier("pointHistorySummaryCache") Cache<PointHistorySummaryCacheKey, PointHistoriesSummaryResponse> cache,
            PointHistoryJpaRepository pointHistoryJpaRepository) {
        this.cache = cache;
        this.pointHistoryJpaRepository = pointHistoryJpaRepository;
    }

    public PointHistoriesSummaryResponse load(PointHistorySummaryCacheKey key) {
        return cache.getIfPresent(key);
    }

    public PointHistoriesSummaryResponse loadAndCache(PointHistorySummaryCacheKey key) {
        return cache.get(key, k -> loadFromDb(key));
    }

    public PointHistoriesSummaryResponse loadFromDb(PointHistorySummaryCacheKey key) {
        List<PointHistoryProjection> rows = pointHistoryJpaRepository.findByDate(key.getUserId(), key.getStartDate(), key.getEndDate());
        List<PointHistorySummaryResponse> response = rows.stream().map(r -> new PointHistorySummaryResponse(r.getIncome(), r.getExpense(), r.getHistoryDate())).toList();
        return new PointHistoriesSummaryResponse(response);
    }
}