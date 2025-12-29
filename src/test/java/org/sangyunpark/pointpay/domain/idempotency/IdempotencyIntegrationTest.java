package org.sangyunpark.pointpay.domain.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.sangyunpark.pointpay.domain.order.entity.OrderStatus;
import org.sangyunpark.pointpay.domain.order.repository.OrderJpaRepository;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class IdempotencyIntegrationTest extends CommonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Autowired
    private RedisTemplate<String, IdempotencyRecord> redisTemplate;

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final String URL_POINT_CHARGE = "/api/point/charge";
    private static final String URL_POINT_PAYMENT = "/api/point/payment";

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
                        post(URL_POINT_CHARGE)
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
                        post(URL_POINT_CHARGE)
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

    @Test
    @DisplayName("같은 멱등키로 두 번 요청시, 결제는 한 번만 진행되고 두 번째 요청은 캐시된 응답을 사용합니다.")
    void whenRequestSameIdempotencyKeyTwice_thenPaymentExactlyOnce() throws Exception {
        User user = new User("test_user", 10_000);
        user = userJpaRepository.save(user);
        long userId = user.getId();

        Order order = Order.builder()
                .user(user)
                .totalAmount(3_000L)
                .status(OrderStatus.CREATED)
                .build();
        order = orderJpaRepository.save(order);
        long orderId = order.getId();

        String idempotencyKey = UUID.randomUUID().toString();
        String requestJson = """
            {
              "userId": %d,
              "orderId": %d
            }
            """.formatted(userId, orderId);

        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;

        MvcResult firstResult = mockMvc.perform(
                        post(URL_POINT_PAYMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                                .content(requestJson)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String firstBody = firstResult.getResponse().getContentAsString();

        IdempotencyRecord redisSaved = redisTemplate.opsForValue().get(redisKey);
        assertThat(redisSaved).isNotNull();
        assertThat(redisSaved.getResponseBody()).isEqualTo(firstBody);
        assertThat(redisSaved.getStatusCode()).isEqualTo(firstResult.getResponse().getStatus());

        MvcResult secondResult = mockMvc.perform(
                        post(URL_POINT_PAYMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                                .content(requestJson)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String secondBody = secondResult.getResponse().getContentAsString();

        assertThat(secondBody).isEqualTo(firstBody);

        User findUser = userJpaRepository.findById(userId).orElseThrow();
        List<PointHistory> histories = pointHistoryJpaRepository.findAll();

        assertThat(histories.get(0).getBalanceBefore()).isEqualTo(10_000L);
        assertThat(histories.get(0).getBalanceAfter()).isEqualTo(7_000L);
        assertThat(findUser.getPointBalance()).isEqualTo(7_000);
    }


    @Test
    @DisplayName("결제가 실패하면, 멱등키에 FAILED 응답이 저장된다")
    void whenPaymentFailed_thenStoreFailedRecord() throws Exception {

        // given
        User user = userJpaRepository.save(new User("test_user", 1_000));
        Order order = orderJpaRepository.save(
                Order.builder()
                        .user(user)
                        .totalAmount(3_000L)
                        .status(OrderStatus.CREATED)
                        .build()
        );

        String idempotencyKey = UUID.randomUUID().toString();
        String requestJson = """
            {
              "userId": %d,
              "orderId": %d
            }
            """.formatted(user.getId(), order.getId());

        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        int expectedStatus = ErrorCode.PAY_POINT_FAILED.getStatus().value();

        // when
        MvcResult result = mockMvc.perform(
                        post(URL_POINT_PAYMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                                .content(requestJson)
                )
                .andExpect(status().is(expectedStatus))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        MvcResult secondResult = mockMvc.perform(
                        post(URL_POINT_PAYMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                                .content(requestJson)
                )
                .andExpect(status().is(expectedStatus))
                .andReturn();

        String secondBody = secondResult.getResponse().getContentAsString();

        assertThat(secondBody).isEqualTo(responseBody);

        IdempotencyRecord savedAfterSecond = redisTemplate.opsForValue().get(redisKey);
        assertThat(savedAfterSecond).isNotNull();
        assertThat(savedAfterSecond.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(savedAfterSecond.getResponseBody()).isEqualTo(responseBody);

        Order reloadedOrderAfterSecond = orderJpaRepository.findById(order.getId()).orElseThrow();
        assertThat(reloadedOrderAfterSecond.getStatus()).isEqualTo(OrderStatus.CREATED);

        User reloadedUserAfterSecond = userJpaRepository.findById(user.getId()).orElseThrow();
        assertThat(reloadedUserAfterSecond.getPointBalance()).isEqualTo(1_000L);

        assertThat(pointHistoryJpaRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    void clean() {
        try (RedisConnection con = redisConnectionFactory.getConnection()) {
            con.serverCommands().flushDb();
        }
        dbCleaner.clear();
    }
}