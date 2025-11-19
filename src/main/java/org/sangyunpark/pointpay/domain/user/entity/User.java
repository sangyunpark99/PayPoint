package org.sangyunpark.pointpay.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
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

    private static final int MAX_POINT = 2_100_000_000;

    protected User() {}

    public User(final long userId) {
        this.id = userId;
    }

    public User(final String username, final int pointBalance) {
        this.username = username;
        this.pointBalance = pointBalance;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return this.id;
    }

    public int getPointBalance() {
        return this.pointBalance;
    }

    public void chargePoint(final int addPointBalance) {
        if(addPointBalance <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        if(this.pointBalance + addPointBalance > MAX_POINT) {
            throw new IllegalArgumentException("최대 포인트 한도를 초과합니다.");
        }

        this.pointBalance += addPointBalance;
    }

    public void usePoint(final int minusPointBalance) {
        if(this.pointBalance < minusPointBalance) {
            throw new IllegalArgumentException("잔여 포인트가 부족합니다.");
        }

        this.pointBalance -= pointBalance;
    }
 }
