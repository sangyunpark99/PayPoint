package org.sangyunpark.pointpay.domain.idempotency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.point.service.PointService;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class IdempotencyIntegrationTest extends CommonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RedisTemplate<String, IdempotencyRecord> redisTemplate;

    @Autowired
    private PointService pointService;

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final String URL = "/api/point/charge";

    @Test
    @DisplayName("같은 멱등키로 두 번 요청시, 포인트는 한 번만 충전되고 두 번째 요청은 캐시된 응답을 사용합니다.")
    void chargePointIdempotentWithRedisStore() throws Exception {
        User user = new User("test", 0);
        User savedUser = userJpaRepository.save(user);
        long userId = savedUser.getId();
        int amount = 1000;

        String idempotencyKey = UUID.randomUUID().toString();
        String requestJson = """
            {
              "userId": %d,
              "point": %d
            }
            """.formatted(userId, amount);

        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.delete(redisKey);

        MvcResult firstResult = mockMvc.perform(
                        post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                                .content(requestJson)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String firstBody = firstResult.getResponse().getContentAsString();

        IdempotencyRecord redisSaved = redisTemplate.opsForValue().get(redisKey);
        assertThat(redisSaved.getResponseBody()).isEqualTo(firstBody);

        MvcResult secondResult = mockMvc.perform(
                        post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                                .content(requestJson)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String secondBody = secondResult.getResponse().getContentAsString();

        assertThat(secondBody).isEqualTo(firstBody);

        User findUser = userJpaRepository.findById(userId).orElseThrow();
        assertThat(findUser.getPointBalance()).isEqualTo(amount);
    }
}