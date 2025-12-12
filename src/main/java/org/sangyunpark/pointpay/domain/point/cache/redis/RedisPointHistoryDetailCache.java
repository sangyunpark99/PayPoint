package org.sangyunpark.pointpay.domain.point.cache.redis;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.PointHistoryDetailCache;
import org.sangyunpark.pointpay.domain.point.cache.loader.PointHistoryDetailDbLoader;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.YearMonth;

@Primary
@Component
@RequiredArgsConstructor
public class RedisPointHistoryDetailCache implements PointHistoryDetailCache<PointHistoryDetailCacheKey>{

    private static final String KEY_FORMAT = "point:detail:%d:%s";
    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, PointHistoriesResponse> redisTemplate;
    private final PointHistoryDetailDbLoader loader;

    private String buildKey(PointHistoryDetailCacheKey key) {
        long userId = key.getUserId();
        YearMonth yearMonth = key.getYearMonth();
        return String.format(KEY_FORMAT, userId, yearMonth);
    }

    @Override
    public PointHistoriesResponse load(PointHistoryDetailCacheKey key) {
        return redisTemplate.opsForValue().get(buildKey(key));
    }

    @Override
    public PointHistoriesResponse loadAndCache(PointHistoryDetailCacheKey key) {
        PointHistoriesResponse value = load(key);
        if(value != null) {
            return  value;
        }

        PointHistoriesResponse loaded = loader.loadFromDbForMonth(key);
        String redisKey = buildKey(key);
        redisTemplate.opsForValue().set(redisKey, loaded, TTL);
        return loaded;
    }

    @Override
    public void evict(PointHistoryDetailCacheKey key) {
        redisTemplate.delete(buildKey(key));
    }
}
