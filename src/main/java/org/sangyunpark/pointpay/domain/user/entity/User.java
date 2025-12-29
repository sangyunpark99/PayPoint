package org.sangyunpark.pointpay.domain.user.entity;

import jakarta.persistence.*;
import org.sangyunpark.pointpay.common.BaseEntity;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private long pointBalance;

    @OneToMany(mappedBy = "user")
    private List<PointHistory> pointHistories = new ArrayList<>();

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

    public long getPointBalance() {
        return this.pointBalance;
    }

    public void validatePointCharge(int pointAmount, int maxPoint) {
        if(this.pointBalance + pointAmount >= maxPoint) {
            throw new BusinessException(ErrorCode.POINT_LIMIT_EXCEED);
        }
    }
 }
