package org.sangyunpark.pointpay.domain.order.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.order.controller.dto.request.OrderCreateRequest;
import org.sangyunpark.pointpay.domain.order.controller.dto.response.OrderCreateResponse;
import org.sangyunpark.pointpay.domain.order.controller.dto.response.OrderDetailResponse;
import org.sangyunpark.pointpay.domain.order.entity.OrderStatus;
import org.sangyunpark.pointpay.domain.order.repository.OrderItemJpaRepository;
import org.sangyunpark.pointpay.domain.order.repository.OrderJpaRepository;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.sangyunpark.pointpay.domain.product.repository.ProductJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderServiceTest extends CommonTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("주문 생성시, 상품의 재고가 차감됩니다.")
    void whenCreateOrder_thenDecreaseProductStock() {
        // given
        User savedUser = userJpaRepository.save(new User("test", 1000));
        Product savedProduct = productJpaRepository.save(new Product("test_상품", 2, 1000));
        int orderQty = 2;

        List<OrderCreateRequest.Item> items = List.of(
                new OrderCreateRequest.Item(savedProduct.getId(), orderQty)
        );

        // when
        OrderCreateResponse order = orderService.createOrder(savedUser.getId(), items);

        entityManager.clear();
        Product updatedProduct = productJpaRepository.findById(savedProduct.getId()).orElseThrow();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualTo((long) savedProduct.getPrice() * orderQty);
        assertThat(updatedProduct.getStock()).isEqualTo(0);
    }

    @Test
    @Transactional
    @DisplayName("주문 생성시, 상품의 재고가 부족한 경우 OUT_OF_STOCK 예외를 발생시킵니다.")
    void whenCreateOrder_thenThrowOutOfStocksException() {
        // given
        User savedUser = userJpaRepository.save(new User("test", 1000));
        Product savedProduct = productJpaRepository.save(new Product("test_상품", 1, 1000));
        int orderQty = 2;

        List<OrderCreateRequest.Item> items = List.of(
                new OrderCreateRequest.Item(savedProduct.getId(), orderQty)
        );

        // when, then
        assertThatThrownBy(() -> orderService.createOrder(savedUser.getId(), items))
                .isInstanceOf(BusinessException.class)
                .extracting("errorMessage")
                .isEqualTo(ErrorCode.OUT_OF_STOCK.getDescription());
    }

    @Test
    @Transactional
    @DisplayName("주문 아이디로 주문을 조회합니다.")
    void whenGetOrderByOrderId_thenGetOrder() {
        // given
        User savedUser = userJpaRepository.save(new User("test", 1000));
        Product savedProduct = productJpaRepository.save(new Product("test_상품", 1, 1000));
        int qty = 1;

        List<OrderCreateRequest.Item> items = List.of(
                new OrderCreateRequest.Item(savedProduct.getId(), qty)
        );

        OrderCreateResponse created = orderService.createOrder(savedUser.getId(), items);
        long orderId = created.getOrderId();

        entityManager.clear();

        // when
        OrderDetailResponse found = orderService.getOrder(orderId);

        // then
        assertThat(found.getOrderId()).isEqualTo(orderId);
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(found.getTotalAmount()).isEqualTo((long) savedProduct.getPrice() * qty);

        assertThat(found.getItems()).hasSize(1);

        OrderDetailResponse.Item item = found.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(savedProduct.getId());
        assertThat(item.getProductName()).isEqualTo(savedProduct.getName());
        assertThat(item.getUnitPrice()).isEqualTo(savedProduct.getPrice());
        assertThat(item.getQuantity()).isEqualTo(qty);
        assertThat(item.getLineAmount()).isEqualTo((long) savedProduct.getPrice() * qty);
    }

    @Test
    @DisplayName("다건 주문에서 일부 상품 재고가 부족하면 전체가 롤백되어 재고 및 주문이 되지 않습니다.")
    void whenOneItemOutOfStock_thenRollbackAll() {
        // given
        User user = userJpaRepository.save(new User("u1", 1_000_000));

        Product p1 = productJpaRepository.save(new Product("p1", 10, 1000));
        Product p2 = productJpaRepository.save(new Product("p2", 0, 1000));   // 재고 부족
        Product p3 = productJpaRepository.save(new Product("p3", 10, 1000));

        int qty = 1;

        List<OrderCreateRequest.Item> items = List.of(
                new OrderCreateRequest.Item(p1.getId(), qty),
                new OrderCreateRequest.Item(p2.getId(), qty),
                new OrderCreateRequest.Item(p3.getId(), qty)
        );

        long p1StockBefore = p1.getStock();
        long p2StockBefore = p2.getStock();
        long p3StockBefore = p3.getStock();

        // when
        assertThatThrownBy(() -> orderService.createOrder(user.getId(), items))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OUT_OF_STOCK);

        // then
        entityManager.clear();

        Product p1After = productJpaRepository.findById(p1.getId()).orElseThrow();
        Product p2After = productJpaRepository.findById(p2.getId()).orElseThrow();
        Product p3After = productJpaRepository.findById(p3.getId()).orElseThrow();

        assertThat(p1After.getStock()).isEqualTo(p1StockBefore);
        assertThat(p2After.getStock()).isEqualTo(p2StockBefore);
        assertThat(p3After.getStock()).isEqualTo(p3StockBefore);

        assertThat(orderJpaRepository.count()).isEqualTo(0);
        assertThat(orderItemJpaRepository.count()).isEqualTo(0);
    }

    @AfterEach
    void clear() {
        dbCleaner.clear();
    }
}