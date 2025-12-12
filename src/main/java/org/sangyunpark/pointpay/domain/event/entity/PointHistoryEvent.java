package org.sangyunpark.pointpay.domain.event.entity;

import jakarta.persistence.*;
import lombok.Builder;
import org.sangyunpark.pointpay.common.BaseEntity;
import org.sangyunpark.pointpay.domain.point.entity.PointType;

import java.time.LocalDateTime;

@Entity
public class PointHistoryEvent extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int pointAmount;

    @Enumerated(EnumType.STRING)
    private PointType pointType;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;

    protected PointHistoryEvent() {
    }

    @Builder
    public PointHistoryEvent(Long userId, int pointAmount, PointType pointType, LocalDateTime createdAt) {
        this.userId = userId;
        this.pointAmount = pointAmount;
        this.pointType = pointType;
        this.eventStatus = EventStatus.READY;
        this.createdAt = createdAt;
    }

    public void updateEventStatus(EventStatus status) {
        this.eventStatus = status;
    }

    public PointType getPointType() {
        return this.pointType;
    }

    public int getAmount() {
        return this.pointAmount;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public long getUserId() {
        return this.userId;
    }
}