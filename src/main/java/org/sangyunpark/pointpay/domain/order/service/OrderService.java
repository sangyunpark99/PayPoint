package org.sangyunpark.pointpay.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.order.controller.dto.request.OrderCreateRequest;
import org.sangyunpark.pointpay.domain.order.controller.dto.response.OrderCreateResponse;
import org.sangyunpark.pointpay.domain.order.controller.dto.response.OrderDetailResponse;
import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.sangyunpark.pointpay.domain.order.entity.OrderItem;
import org.sangyunpark.pointpay.domain.order.repository.OrderJpaRepository;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.sangyunpark.pointpay.domain.product.repository.ProductJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderJpaRepository orderJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    @Transactional
    public OrderCreateResponse createOrder(long userId, List<OrderCreateRequest.Item> items) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Map<Long, Integer> quantityMap = items.stream()
                .collect(Collectors.toMap(
                        OrderCreateRequest.Item::getProductId,
                        OrderCreateRequest.Item::getQuantity,
                        Integer::sum
                ));

        List<Long> ids = quantityMap.keySet().stream()
                .sorted() // 데드락 방지
                .toList();

        List<Product> products = productJpaRepository.findAllById(ids); // 순서 보장이 안됨
        if (products.size() != ids.size()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        long totalAmount = 0;
        Order order = Order.create(user, 0);

        for (Long productId : ids) {
            int quantity = quantityMap.get(productId);
            int updated = productJpaRepository.decreaseStockIfEnough(productId, quantity);
            if (updated == 0) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }
            Product product = productMap.get(productId);
            int quantityPerProduct = quantityMap.get(productId);
            OrderItem orderItem = OrderItem.fromProduct(product, quantityPerProduct);
            totalAmount += orderItem.getUnitPrice() * orderItem.getQuantity();
            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderJpaRepository.save(order);
        return OrderCreateResponse.fromOrder(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(long orderId) {
        Order order = orderJpaRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return OrderDetailResponse.from(order);
    }
}
