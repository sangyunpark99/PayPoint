package org.sangyunpark.pointpay.config.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, PointHistoriesPerMonthResponse> pointHistoryPerMonthRedisTemplate(ObjectMapper redisObjectMapper) {
        RedisTemplate<String, PointHistoriesPerMonthResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        GzipRedisSerializer<PointHistoriesPerMonthResponse> gzipSerializer = new GzipRedisSerializer<>(redisObjectMapper, new TypeReference<>(){});
        template.setValueSerializer(gzipSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(gzipSerializer);

        return template;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        return template;
    }
}