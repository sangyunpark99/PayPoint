package org.sangyunpark.pointpay.domain.point.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.PointHistorySummaryCache;
import org.sangyunpark.pointpay.domain.point.cache.loader.PointHistorySummaryDbLoader;
import org.sangyunpark.pointpay.domain.point.cache.local.key.PointHistorySummaryCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalPointHistorySummaryCache implements PointHistorySummaryCache {

    private final Cache<PointHistorySummaryCacheKey, PointHistoriesSummaryResponse> cache;
    private final PointHistorySummaryDbLoader loader;

    @Override
    public PointHistoriesSummaryResponse load(PointHistorySummaryCacheKey key) {
        return cache.getIfPresent(key);
    }

    @Override
    public PointHistoriesSummaryResponse loadAndCache(PointHistorySummaryCacheKey key) {
        return cache.get(key, k -> loader.loadFromDb(key));
    }
}