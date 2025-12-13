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
    private static final Duration COMPLETED_TTL = Duration.ofHours(1);
    private static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(10);

    private final RedisTemplate<String, IdempotencyRecord> redisTemplate;

    public boolean progress(String idempotencyKey) {
        String redisKey = buildKey(idempotencyKey);
        IdempotencyRecord record = IdempotencyRecord.inProgress();
        Boolean check = redisTemplate.opsForValue().setIfAbsent(redisKey, record, IN_PROGRESS_TTL);
        return Boolean.TRUE.equals(check);
    }

    private String buildKey(String idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }

    public Optional<IdempotencyRecord> find(String idempotencyKey) {
        IdempotencyRecord record = redisTemplate.opsForValue().get(buildKey(idempotencyKey));
        return Optional.ofNullable(record);
    }

    public void complete(String idempotencyKey, IdempotencyRecord record) {
        redisTemplate.opsForValue().set(buildKey(idempotencyKey), record, COMPLETED_TTL);
    }
}
