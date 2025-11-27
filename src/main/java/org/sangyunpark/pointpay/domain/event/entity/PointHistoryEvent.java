package org.sangyunpark.pointpay.domain.event.entity;

import jakarta.persistence.*;
import lombok.Builder;
import org.sangyunpark.pointpay.common.BaseEntity;
import org.sangyunpark.pointpay.domain.point.entity.PointType;

@Entity
public class PointHistoryEvent extends BaseEntity{

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
    public PointHistoryEvent(Long userId, int pointAmount, PointType pointType) {
        this.userId = userId;
        this.pointAmount = pointAmount;
        this.pointType = pointType;
        this.eventStatus = EventStatus.READY;
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

    public long getUserId() {
        return this.userId;
    }
}