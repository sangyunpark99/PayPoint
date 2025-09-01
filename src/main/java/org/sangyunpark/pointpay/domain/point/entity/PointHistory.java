package org.sangyunpark.pointpay.domain.point.entity;

import jakarta.persistence.*;
import org.sangyunpark.pointpay.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
public class PointHistory {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private PointStatus pointStatus;

    private LocalDateTime createdAt;

    protected PointHistory() {}

    public PointHistory(final User user, final PointStatus pointStatus) {
        this.user = user;
        this.pointStatus = pointStatus;
        this.createdAt = LocalDateTime.now();
    }
}
