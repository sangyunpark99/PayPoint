package org.sangyunpark.pointpay.check;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.RepositoryTest;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public class HibernateFlushOrderTest extends RepositoryTest{


    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager em;

    @Test
    @Transactional
    void whenUpdateAndInsert_thenFlushOrder() throws NoSuchFieldException, IllegalAccessException {
        User u = userJpaRepository.save(new User("u", 0));
        em.flush();
        em.clear();

        User managed = em.find(User.class, u.getId());

        var f = User.class.getDeclaredField("pointBalance");
        f.setAccessible(true);
        f.setInt(managed, f.getInt(managed) + 100);

        PointHistory ph = new PointHistory(managed,PointType.CHARGE,100, LocalDateTime.now());
        em.persist(ph);

        em.flush();
        em.clear();
    }
}
