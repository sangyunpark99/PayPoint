package org.sangyunpark.pointpay.domain.payment.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyRecord;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyStatus;
import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.sangyunpark.pointpay.domain.order.entity.OrderStatus;
import org.sangyunpark.pointpay.domain.order.repository.OrderJpaRepository;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class PaymentIntegrationTest extends CommonTest {

    @Autowired
    private MockMvc mockMvc;

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
    private static final String URL_POINT_PAYMENT = "/api/point/payment";

    @BeforeEach
    void before() {
        dbCleaner.clear();
    }

    @Test
    @DisplayName("결제 1회 요청 성공 -> 주문 PAID, 포인트 차감 1회 멱등 응답 저장")
    void whenPaymentOnce_thenDbAndRedisAreConsistent() throws Exception {
        // given
        User user = userJpaRepository.save(new User("test_user", 10_000));
        long userId = user.getId();

        Order order = orderJpaRepository.save(
                Order.builder()
                        .user(user)
                        .totalAmount(3_000L)
                        .status(OrderStatus.CREATED)
                        .build()
        );
        long orderId = order.getId();

        String idempotencyKey = UUID.randomUUID().toString();
        String requestJson = """
        { "userId": %d, "orderId": %d }
        """.formatted(userId, orderId);

        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.delete(redisKey);

        // when
        MvcResult result = mockMvc.perform(
                        post(URL_POINT_PAYMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                                .content(requestJson)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();

        // then
        Order reloadedOrder = orderJpaRepository.findById(orderId).orElseThrow();
        assertThat(reloadedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        User reloadedUser = userJpaRepository.findById(userId).orElseThrow();
        assertThat(reloadedUser.getPointBalance()).isEqualTo(7_000L);

        PointHistory ph = pointHistoryJpaRepository.findAll().get(0);
        assertThat(ph.getPointType()).isEqualTo(PointType.PAYMENT);
        assertThat(ph.getBalanceBefore()).isEqualTo(10_000L);
        assertThat(ph.getBalanceAfter()).isEqualTo(7_000L);

        IdempotencyRecord saved = redisTemplate.opsForValue().get(redisKey);
        assertThat(saved).isNotNull();
        assertThat(saved.getStatusCode()).isEqualTo(statusCode);
        assertThat(saved.getResponseBody()).isEqualTo(body);
        assertThat(saved.getStatus()).isEqualTo(IdempotencyStatus.SUCCESS);

        Long ttlSeconds = redisTemplate.getExpire(redisKey);
        assertThat(ttlSeconds).isNotNull();
        assertThat(ttlSeconds).isGreaterThan(0);
    }
}
