package org.sangyunpark.pointpay.domain.point.cache.redis;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.PointHistoryDetailCache;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailTodayCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.loader.PointHistoryDetailDbLoader;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisPointHistoryDetailTodayCache implements PointHistoryDetailCache<PointHistoryDetailTodayCacheKey> {

    private static final Duration TTL = Duration.ofHours(1);
    private static final String KEY_FORMAT = "point:detail:%d:%s";

    private final RedisTemplate<String, PointHistoriesResponse> redisTemplate;
    private final PointHistoryDetailDbLoader loader;

    private String buildKey(PointHistoryDetailTodayCacheKey key) {
        return String.format(KEY_FORMAT, key.getUserId(), key.getCreatedAt());
    }

    @Override
    public PointHistoriesResponse load(PointHistoryDetailTodayCacheKey key) {
        return redisTemplate.opsForValue().get(buildKey(key));
    }

    @Override
    public PointHistoriesResponse loadAndCache(PointHistoryDetailTodayCacheKey key) {
        PointHistoriesResponse value = load(key);
        if(value != null) {
            return  value;
        }

        PointHistoriesResponse loaded = loader.loadFromDbForToday(key);
        String redisKey = buildKey(key);
        redisTemplate.opsForValue().set(redisKey, loaded, TTL);
        return loaded;
    }

    @Override
    public void evict(PointHistoryDetailTodayCacheKey key) {
        redisTemplate.delete(buildKey(key));
    }
}
