package org.sangyunpark.pointpay.domain.user.repository;

import org.sangyunpark.pointpay.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
}
