package org.sangyunpark.pointpay.user.entity;

import jakarta.persistence.*;
import org.sangyunpark.pointpay.point.entity.PointHistory;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private int pointBalance;

    @OneToMany(mappedBy = "user")
    private List<PointHistory> pointHistories = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    protected User() {}

    public User(final String username, final int pointBalance) {
        this.username = username;
        this.pointBalance = pointBalance;
        this.createdAt = LocalDateTime.now();
    }
}
