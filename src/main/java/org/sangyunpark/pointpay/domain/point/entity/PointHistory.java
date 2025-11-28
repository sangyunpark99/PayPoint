package org.sangyunpark.pointpay.domain.point.entity;

import jakarta.persistence.*;
import lombok.Builder;
import org.sangyunpark.pointpay.common.BaseEntity;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
public class PointHistory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private PointType pointType;

    @Enumerated(EnumType.STRING)
    private PointStatus pointStatus;

    private int pointBalance;

    protected PointHistory() {}

    @Builder
    public PointHistory(User user, PointType pointType, int pointBalance, PointStatus pointStatus) {
        this.user = user;
        this.pointType = pointType;
        this.createdAt = LocalDateTime.now();
        this.pointBalance = pointBalance;
        this.pointStatus = pointStatus;
    }

    public User getUser() {
        return this.user;
    }

    public int getPointBalance() {
        return this.pointBalance;
    }

    public PointStatus getPointStatus() {
        return this.pointStatus;
    }

    public PointType getPointType() {
        return this.pointType;
    }

    public static PointHistory fromEvent(PointHistoryEvent event, User user) {
        return PointHistory.builder()
                .user(user)
                .pointType(event.getPointType())
                .pointBalance(event.getAmount())
                .pointStatus(PointStatus.SUCCESS)
                .build();
    }
}