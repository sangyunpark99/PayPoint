package org.sangyunpark.pointpay.domain.point.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.sangyunpark.pointpay.common.BaseEntity;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
public class PointHistory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private PointType pointType;

    private long balanceBefore;

    private long balanceAfter;

    private LocalDate historyDate;

    protected PointHistory() {}

    @Builder
    public PointHistory(User user, PointType pointType, long balanceBefore, long balanceAfter, LocalDateTime createdAt) {
        this.user = user;
        this.pointType = pointType;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
        this.historyDate = LocalDateTime.now().toLocalDate();
    }

    public static PointHistory fromEvent(PointHistoryEvent event, User user) {
        return PointHistory.builder()
                .user(user)
                .pointType(event.getPointType())
                .balanceBefore(event.getBalanceBefore())
                .balanceAfter(event.getBalanceAfter())
                .createdAt(event.getCreatedAt())
                .build();
    }
}