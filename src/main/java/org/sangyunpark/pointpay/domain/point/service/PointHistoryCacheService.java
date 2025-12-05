package org.sangyunpark.pointpay.domain.point.service;

import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistorySummaryCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.manager.PointHistoryDetailCacheManager;
import org.sangyunpark.pointpay.domain.point.cache.manager.PointHistorySummaryCacheManager;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PointHistoryCacheService {

    private final PointHistorySummaryCacheManager summaryCacheManager;
    private final PointHistoryDetailCacheManager detailCacheManager;

    public PointHistoryCacheService(PointHistorySummaryCacheManager summaryCacheManager, PointHistoryDetailCacheManager detailCacheManager) {
        this.summaryCacheManager = summaryCacheManager;
        this.detailCacheManager = detailCacheManager;
    }

    public PointHistoriesSummaryResponse getSummaryPointHistories(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        PointHistorySummaryCacheKey key = new PointHistorySummaryCacheKey(userId, startDate, endDate);
        PointHistoriesSummaryResponse response = summaryCacheManager.load(key);
        if(response != null) {
            return response;
        }
        return summaryCacheManager.loadAndCache(key);
    }

    public PointHistoriesPerMonthResponse getDetailPointHistoriesPerMonth(long userId, LocalDateTime startDate) {
        PointHistoryDetailCacheKey key = new PointHistoryDetailCacheKey(userId, startDate);
        PointHistoriesPerMonthResponse response = detailCacheManager.load(key);
        if(response != null) {
            return response;
        }

        return detailCacheManager.loadAndCache(key);
    }
}