package org.sangyunpark.pointpay.domain.point.service;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.PointHistoryDetailCache;
import org.sangyunpark.pointpay.domain.point.cache.PointHistorySummaryCache;
import org.sangyunpark.pointpay.domain.point.cache.local.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.local.key.PointHistorySummaryCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointHistoryCacheService {

    private final PointHistoryDetailCache detailCache;

    private final PointHistorySummaryCache summaryCache;

    public PointHistoriesSummaryResponse getSummaryPointHistories(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        PointHistorySummaryCacheKey key = new PointHistorySummaryCacheKey(userId, startDate, endDate);
        PointHistoriesSummaryResponse response = summaryCache.load(key);
        if(response != null) {
            return response;
        }
        return summaryCache.loadAndCache(key);
    }

    public PointHistoriesPerMonthResponse getDetailPointHistoriesPerMonth(long userId, LocalDateTime startDate) {
        PointHistoryDetailCacheKey key = new PointHistoryDetailCacheKey(userId, startDate);
        PointHistoriesPerMonthResponse response = detailCache.load(key);
        if(response != null) {
            return response;
        }

        return detailCache.loadAndCache(key);
    }
}