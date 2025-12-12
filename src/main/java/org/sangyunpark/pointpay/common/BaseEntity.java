package org.sangyunpark.pointpay.common;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    @CreatedDate
    protected LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if(createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
