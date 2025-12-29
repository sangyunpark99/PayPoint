package org.sangyunpark.pointpay.check;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

public class JpaTest extends CommonTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private TransactionTemplate tx;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    void whenSelectedStartQuery_thenFlushFindTime() {
        // given
        Long id = tx.execute(status -> {
            User u = userJpaRepository.save(new User("u1", 1000));
            em.flush();
            return u.getId();
        });

        // when, then
        tx.executeWithoutResult(status -> {
            em.clear();
            System.out.println("---- before find ----");
            User u = em.find(User.class, id);
            System.out.println("---- after find ----");
        });
    }
}
