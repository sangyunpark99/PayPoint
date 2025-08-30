package org.sangyunpark.pointpay.user.service;

import jakarta.transaction.Transactional;
import org.sangyunpark.pointpay.global.BusinessException;
import org.sangyunpark.pointpay.global.ErrorCode;
import org.sangyunpark.pointpay.user.entity.User;
import org.sangyunpark.pointpay.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void createUser(final String username, final int pointBalance) {

        if(isDuplicateUsername(username)) {
            throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
        }

        final User user = new User(username, pointBalance);
        userRepository.save(user);
    }

    private boolean isDuplicateUsername(final String username) {
        return userRepository.existsByUsername(username);
    }
}
