package org.sangyunpark.pointpay.domain.point.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.point.repository.projection.PointHistoryProjection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse.*;

@Component
public class PointHistoryCacheManager {

    private final Cache<PointHistoryCacheKey, PointHistoriesResponse> cache;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistoryCacheManager(Cache<PointHistoryCacheKey, PointHistoriesResponse> cache, PointHistoryJpaRepository pointHistoryJpaRepository) {
        this.cache = cache;
        this.pointHistoryJpaRepository = pointHistoryJpaRepository;
    }

    public PointHistoriesResponse load(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        PointHistoryCacheKey key = new PointHistoryCacheKey(userId, startDate, endDate);
        return cache.getIfPresent(key);
    }

    public PointHistoriesResponse loadAndCache(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        PointHistoryCacheKey key = new PointHistoryCacheKey(userId, startDate, endDate);
        return cache.get(key, k -> loadFromDb(userId, startDate, endDate));
    }

    public PointHistoriesResponse loadFromDb(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<PointHistoryProjection> rows = pointHistoryJpaRepository.findByDate(userId, startDate, endDate);
        List<PointHistoryResponse> response = rows.stream().map(r -> new PointHistoryResponse(r.getIncome(), r.getExpense(), r.getHistoryDate())).toList();
        return new PointHistoriesResponse(response);
    }
}
