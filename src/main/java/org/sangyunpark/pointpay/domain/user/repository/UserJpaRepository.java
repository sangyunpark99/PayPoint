package org.sangyunpark.pointpay.domain.user.repository;

import org.sangyunpark.pointpay.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE User u set u.pointBalance = u.pointBalance + :amount WHERE u.id = :userId AND u.pointBalance + :amount <= :maxPoint")
    int increasePoint(Long userId, long amount, int maxPoint); // 원자적 업데이트

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE User u SET u.pointBalance = u.pointBalance - :amount WHERE u.id = :userId AND u.pointBalance - :amount >= 0")
    int decreasePoint(Long userId, long amount);

    @Query("SELECT u.pointBalance FROM User u WHERE u.id = :userId")
    long findPointBalanceByUserId(long userId);

    @Query("SELECT u.id FROM User u")
    List<Long> findAllIds();

    @Query("SELECT u.pointBalance FROM User u WHERE u.id = :userId")
    int findPointBalanceById(Long userId);
}
