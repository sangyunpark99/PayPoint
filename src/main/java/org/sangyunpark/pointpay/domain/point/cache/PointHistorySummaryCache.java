package org.sangyunpark.pointpay.domain.point.cache;

import org.sangyunpark.pointpay.domain.point.cache.key.PointHistorySummaryCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;

public interface PointHistorySummaryCache {
    PointHistoriesSummaryResponse load(PointHistorySummaryCacheKey key);
    PointHistoriesSummaryResponse loadAndCache(PointHistorySummaryCacheKey key);
}
