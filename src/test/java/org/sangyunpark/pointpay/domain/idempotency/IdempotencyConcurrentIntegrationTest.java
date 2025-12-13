package org.sangyunpark.pointpay.domain.idempotency;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
public class IdempotencyConcurrentIntegrationTest extends CommonTest {

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";
    private static final String PREFIX = "idempotency:";
    private static final String URL = "/api/point/charge";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RedisTemplate<String, IdempotencyRecord> idempotencyRedisTemplate;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("동일 멱등키로 동시에 2번 요청해도 chargePoint는 1번만 실행되어야 한다")
    void sameIdempotencyKey_twoConcurrentRequests_executeOnlyOnce() throws Exception {
        // given
        User user = userJpaRepository.save(new User("test", 0));
        long userId = user.getId();
        int amount = 1000;

        String key = UUID.randomUUID().toString();
        String body = """
            { "userId": %d, "point": %d }
            """.formatted(userId, amount);

        idempotencyRedisTemplate.delete(PREFIX + key);

        // when
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<Integer> f1 = pool.submit(() -> {
            start.await();
            return mockMvc.perform(
                    post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(IDEMPOTENCY_HEADER, key)
                            .content(body)
            ).andReturn().getResponse().getStatus();
        });

        Future<Integer> f2 = pool.submit(() -> {
            start.await();
            return mockMvc.perform(
                    post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(IDEMPOTENCY_HEADER, key)
                            .content(body)
            ).andReturn().getResponse().getStatus();
        });

        start.countDown();

        int s1 = f1.get(5, TimeUnit.SECONDS);
        int s2 = f2.get(5, TimeUnit.SECONDS);

        // then
        assertThat(List.of(s1, s2)).contains(HttpServletResponse.SC_CONFLICT);
        assertThat(Stream.of(s1, s2).anyMatch(s -> s >= 200 && s < 300)).isTrue();

        em.clear();
        User reloaded = userJpaRepository.findById(userId).orElseThrow();
        assertThat(reloaded.getPointBalance()).isEqualTo(amount);
    }

}
