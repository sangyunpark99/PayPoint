package org.sangyunpark.pointpay.domain.point.service;

import org.sangyunpark.pointpay.domain.point.cache.PointHistoryCacheManager;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PointHistoryCacheService {

    private final PointHistoryCacheManager cacheManager;

    public PointHistoryCacheService(PointHistoryCacheManager manager) {
        this.cacheManager = manager;
    }

    public PointHistoriesResponse getPointHistories(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        PointHistoriesResponse response = cacheManager.load(userId, startDate, endDate);
        if(response != null) {
            return response;
        }
        return cacheManager.loadAndCache(userId, startDate, endDate);
    }
}