package org.sangyunpark.pointpay.domain.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    public static final String KEY_PREFIX = "idempotency:";
    public static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, IdempotencyRecord> redisTemplate;

    private String buildKey(String idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }

    public Optional<IdempotencyRecord> find(String idempotencyKey) {
        IdempotencyRecord record = redisTemplate.opsForValue().get(buildKey(idempotencyKey));
        return Optional.ofNullable(record);
    }

    public void save(String idempotencyKey, IdempotencyRecord record) {
        redisTemplate.opsForValue().set(buildKey(idempotencyKey), record, TTL);
    }
}
