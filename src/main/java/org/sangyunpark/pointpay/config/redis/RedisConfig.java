package org.sangyunpark.pointpay.config.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyRecord;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    private <T> RedisTemplate<String, T> createRedisTemplate(RedisSerializer<T> valueSerializer) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        return template;
    }

    private <T> GzipRedisSerializer<T> gzipSerializer(ObjectMapper objectMapper, TypeReference<T> typeReference) {
        return new GzipRedisSerializer<>(objectMapper, typeReference);
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, PointHistoriesResponse> pointHistoryPerMonthRedisTemplate(ObjectMapper redisObjectMapper) {
        return createRedisTemplate(gzipSerializer(redisObjectMapper, new TypeReference<>(){}));
    }

    @Bean
    public RedisTemplate<String, IdempotencyRecord> idempotencyRedisTemplate(ObjectMapper redisObjectMapper) {
        return createRedisTemplate(gzipSerializer(redisObjectMapper, new TypeReference<>(){}));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(ObjectMapper redisObjectMapper) {
        return createRedisTemplate(gzipSerializer(redisObjectMapper, new TypeReference<>() {}));
    }
}