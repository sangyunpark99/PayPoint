package org.sangyunpark.pointpay.domain.order.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.sangyunpark.pointpay.common.BaseEntity;
import org.sangyunpark.pointpay.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private long totalAmount;

    private String paymentTxId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected Order() {

    }

    @Builder
    public Order(User user, long totalAmount, String paymentTxId, OrderStatus status) {
        this.user = user;
        this.totalAmount = totalAmount;
        this.paymentTxId = paymentTxId;
        this.status = status;
    }
    public static Order create(User user, long totalAmount) {
        return Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .build();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }
}
