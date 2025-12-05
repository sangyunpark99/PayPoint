package org.sangyunpark.pointpay.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.sangyunpark.pointpay.domain.point.cache.PointHistoryCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalCacheConfig {

    @Bean
    public Cache<PointHistoryCacheKey, PointHistoriesResponse> pointHistoryCache() {
        return Caffeine.newBuilder()
                .build();
    }
}
