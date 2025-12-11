package org.sangyunpark.pointpay.domain.point.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.point.cache.PointHistoryDetailCache;
import org.sangyunpark.pointpay.domain.point.cache.loader.PointHistoryDetailDbLoader;
import org.sangyunpark.pointpay.domain.point.cache.local.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalPointHistoryDetailCache implements PointHistoryDetailCache {

    private final Cache<PointHistoryDetailCacheKey, PointHistoriesPerMonthResponse> cache;
    private final PointHistoryDetailDbLoader loader;

    @Override
    public PointHistoriesPerMonthResponse load(PointHistoryDetailCacheKey key) {
        log.info("cache size = {}", cache.asMap().size());
        return cache.getIfPresent(key);
    }

    @Override
    public PointHistoriesPerMonthResponse loadAndCache(PointHistoryDetailCacheKey key) {
        return cache.get(key, v -> loader.loadFromDb(key));
    }
}
