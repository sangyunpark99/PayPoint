package org.sangyunpark.pointpay.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.event.PointHistoryEventCreated;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.event.repository.PointHistoryEventJpaRepository;
import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.sangyunpark.pointpay.domain.order.entity.OrderStatus;
import org.sangyunpark.pointpay.domain.order.repository.OrderJpaRepository;
import org.sangyunpark.pointpay.domain.payment.controller.dto.response.OrderPaymentResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderJpaRepository orderJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PointHistoryEventJpaRepository pointHistoryEventJpaRepository;

    @Transactional
    public OrderPaymentResponse orderPayment(long userId, long orderId) {

        User savedUser = userJpaRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Order savedOrder = orderJpaRepository.findByIdAndUserId(orderId, savedUser.getId()).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        int updatedOrderStatus = orderJpaRepository.updateOrderStatus(savedOrder.getId(), OrderStatus.CREATED, OrderStatus.PAYING);
        if(updatedOrderStatus == 0) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_UPDATE_FAILED_CREATED_PAYING);
        }

        long totalAmount = savedOrder.getTotalAmount();
        int updated = userJpaRepository.decreasePoint(savedUser.getId(), totalAmount);
        if(updated == 0) {
            throw new BusinessException(ErrorCode.PAY_POINT_FAILED);
        }

        long balanceAfter = userJpaRepository.findPointBalanceByUserId(userId);
        long balanceBefore = balanceAfter + totalAmount;

        updatedOrderStatus = orderJpaRepository.updateOrderStatus(savedOrder.getId(), OrderStatus.PAYING, OrderStatus.PAID);
        if(updatedOrderStatus == 0) {
            throw new BusinessException(ErrorCode. ORDER_STATUS_UPDATE_FAILED_PAYING_PAID);
        }

        PointHistoryEvent event = PointHistoryEvent.builder()
                .userId(savedUser.getId())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .pointType(PointType.PAYMENT)
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryEventJpaRepository.save(event);
        applicationEventPublisher.publishEvent(new PointHistoryEventCreated());

        return OrderPaymentResponse.builder()
                .userId(savedUser.getId())
                .orderId(savedOrder.getId())
                .pointBalance(balanceAfter)
                .beforePointBalance(balanceBefore)
                .build();
    }
}