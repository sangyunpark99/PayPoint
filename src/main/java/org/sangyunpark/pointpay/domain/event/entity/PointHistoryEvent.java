package org.sangyunpark.pointpay.domain.event.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.sangyunpark.pointpay.common.BaseEntity;
import org.sangyunpark.pointpay.domain.point.entity.PointType;

import java.time.LocalDateTime;

@Entity
@Getter
public class PointHistoryEvent extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private long balanceBefore;

    private long balanceAfter;

    @Enumerated(EnumType.STRING)
    private PointType pointType;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;

    protected PointHistoryEvent() {
    }

    @Builder
    public PointHistoryEvent(Long userId, long balanceBefore, long balanceAfter, PointType pointType, LocalDateTime createdAt) {
        this.userId = userId;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.pointType = pointType;
        this.eventStatus = EventStatus.READY;
        this.createdAt = createdAt;
    }
}