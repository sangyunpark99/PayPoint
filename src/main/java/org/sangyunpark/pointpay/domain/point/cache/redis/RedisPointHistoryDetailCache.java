package org.sangyunpark.pointpay.domain.point.cache.redis;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.PointHistoryDetailCache;
import org.sangyunpark.pointpay.domain.point.cache.loader.PointHistoryDetailDbLoader;
import org.sangyunpark.pointpay.domain.point.cache.local.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.YearMonth;

@Primary
@Component
@RequiredArgsConstructor
public class RedisPointHistoryDetailCache implements PointHistoryDetailCache{

    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, PointHistoriesPerMonthResponse> redisTemplate;
    private final PointHistoryDetailDbLoader loader;

    private String buildKey(PointHistoryDetailCacheKey key) {
        long userId = key.getUserId();
        YearMonth yearMonth = key.getYearMonth();
        return "point:detail:" + userId + ":" + yearMonth;
    }

    @Override
    public PointHistoriesPerMonthResponse load(PointHistoryDetailCacheKey key) {
        return redisTemplate.opsForValue().get(buildKey(key));
    }

    @Override
    public PointHistoriesPerMonthResponse loadAndCache(PointHistoryDetailCacheKey key) {
        PointHistoriesPerMonthResponse value = load(key);
        if(value != null) {
            return  value;
        }

        PointHistoriesPerMonthResponse loaded = loader.loadFromDb(key);
        String redisKey = buildKey(key);
        redisTemplate.opsForValue().set(redisKey, loaded, TTL);
        return loaded;
    }
}
