package org.sangyunpark.pointpay.domain.point.cache;

import org.sangyunpark.pointpay.domain.point.cache.local.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;

public interface PointHistoryDetailCache {
    PointHistoriesPerMonthResponse load(PointHistoryDetailCacheKey key);
    PointHistoriesPerMonthResponse loadAndCache(PointHistoryDetailCacheKey key);
}
