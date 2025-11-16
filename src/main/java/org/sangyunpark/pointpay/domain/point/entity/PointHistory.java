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
    private PointStatus pointStatus;

    private LocalDateTime createdAt;

    private int pointBalance;

    protected PointHistory() {}

    public PointHistory(final User user, final PointStatus pointStatus, final int pointBalance) {
        this.user = user;
        this.pointStatus = pointStatus;
        this.createdAt = LocalDateTime.now();
        this.pointBalance = pointBalance;
    }
}
