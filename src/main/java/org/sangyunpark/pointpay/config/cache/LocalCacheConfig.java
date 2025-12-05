package org.sangyunpark.pointpay.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistorySummaryCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalCacheConfig {

    @Bean
    @Qualifier("pointHistorySummaryCache")
    public Cache<PointHistorySummaryCacheKey, PointHistoriesSummaryResponse> pointHistorySummaryCache() {
        return Caffeine.newBuilder()
                .build();
    }

    @Bean
    @Qualifier("pointHistoryDetailCache")
    public Cache<PointHistoryDetailCacheKey, PointHistoriesPerMonthResponse> pointHistoryDetailCache() {
        return Caffeine.newBuilder()
                .build();
    }
}
