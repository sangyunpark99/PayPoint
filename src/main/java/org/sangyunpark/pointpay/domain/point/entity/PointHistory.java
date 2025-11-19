package org.sangyunpark.pointpay.domain.point.entity;

import jakarta.persistence.*;
import org.sangyunpark.pointpay.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
public class PointHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private PointType pointType;

    @Enumerated(EnumType.STRING)
    private PointStatus pointStatus;

    private LocalDateTime createdAt;

    private int pointBalance;

    protected PointHistory() {}

    public PointHistory(final User user, final PointType type, final int pointBalance, final PointStatus status) {
        this.user = user;
        this.pointType = type;
        this.createdAt = LocalDateTime.now();
        this.pointBalance = pointBalance;
        this.pointStatus = status;
    }

    public long getUserId() {
        return this.user.getId();
    }

    public int getPointBalance() {
        return this.pointBalance;
    }

    public void updateStatus(PointStatus status) {
        this.pointStatus = status;
    }
}
