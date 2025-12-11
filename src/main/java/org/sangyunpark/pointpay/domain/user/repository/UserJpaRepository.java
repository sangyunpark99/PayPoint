package org.sangyunpark.pointpay.domain.user.repository;

import org.sangyunpark.pointpay.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE User u set u.pointBalance = u.pointBalance + :amount WHERE u.id = :userId AND u.pointBalance + :amount <= :maxPoint")
    int increasePoint(Long userId, int amount, int maxPoint);

    @Query("SELECT u.id FROM User u")
    List<Long> findAllIds();
}
