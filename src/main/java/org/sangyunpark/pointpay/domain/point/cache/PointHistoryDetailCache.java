package org.sangyunpark.pointpay.domain.point.cache;

import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;

public interface PointHistoryDetailCache<T> {
    PointHistoriesResponse load(T key);
    PointHistoriesResponse loadAndCache(T key);
    void evict(T key);
}
