package org.sangyunpark.pointpay.domain.user.service;

import jakarta.transaction.Transactional;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;

    public UserService(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Transactional
    public void createUser(final String username, final int pointBalance) {

        if(isDuplicateUsername(username)) {
            throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
        }

        final User user = new User(username, pointBalance);
        userJpaRepository.save(user);
    }

    private boolean isDuplicateUsername(final String username) {
        return userJpaRepository.existsByUsername(username);
    }
}
