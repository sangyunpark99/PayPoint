package org.sangyunpark.pointpay.check;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisTest extends CommonTest {

    @Autowired
    private StringRedisTemplate redis;

    private final String key = "setnx:test:key";

    @BeforeEach
    void setUp() {
        redis.delete(key);
    }

    @Test
    void whenKeyExists_thenSetNxReturns1() {

        // 키가 없는 경우 true
        Boolean first = redis.opsForValue().setIfAbsent(key, "Hello");
        int firstResult = Boolean.TRUE.equals(first) ? 1 : 0;
        assertThat(firstResult).isEqualTo(1);

        // 키가 이미 있는 경우 false;
        Boolean second = redis.opsForValue().setIfAbsent(key, "World");
        int secondResult = Boolean.TRUE.equals(second) ? 1 : 0;
        assertThat(secondResult).isEqualTo(0);

        // 값이 덮어씌여지지 않은지 확인
        assertThat(redis.opsForValue().get(key)).isEqualTo("Hello");
    }
}