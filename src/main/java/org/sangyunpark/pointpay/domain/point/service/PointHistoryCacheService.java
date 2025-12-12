package org.sangyunpark.pointpay.domain.point.service;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.PointHistoryDetailCache;
import org.sangyunpark.pointpay.domain.point.cache.PointHistorySummaryCache;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailTodayCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistorySummaryCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.redis.RedisPointHistoryDetailTodayCache;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointHistoryCacheService {

    private final RedisPointHistoryDetailTodayCache detailTodayCache;
    private final PointHistoryDetailCache detailCache;
    private final PointHistorySummaryCache summaryCache;

    public PointHistoriesSummaryResponse getSummaryPointHistories(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        PointHistorySummaryCacheKey key = new PointHistorySummaryCacheKey(userId, startDate, endDate);
        return summaryCache.loadAndCache(key);
    }

    public PointHistoriesResponse getDetailPointHistoriesPerMonth(long userId, LocalDateTime startDate) {
        PointHistoryDetailCacheKey key = new PointHistoryDetailCacheKey(userId, startDate);
        PointHistoriesResponse beforeTodayHistory = detailCache.loadAndCache(key);
        PointHistoriesResponse todayHistory = getDetailPointHistoriesPerToday(userId);
        beforeTodayHistory.getPointHistories().addAll(todayHistory.getPointHistories());
        return beforeTodayHistory;
    }

    public PointHistoriesResponse getDetailPointHistoriesPerToday(long userId) {
        PointHistoryDetailTodayCacheKey key = new PointHistoryDetailTodayCacheKey(userId);
        return detailTodayCache.loadAndCache(key);
    }

    public void detailPointHistoryPerTodayEvict(long userId) {
        PointHistoryDetailTodayCacheKey key = new PointHistoryDetailTodayCacheKey(userId);
        detailTodayCache.evict(key);
    }
}