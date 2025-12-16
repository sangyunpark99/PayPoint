package org.sangyunpark.pointpay.check;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class IdentityInsertTimingTest extends CommonTest {

    private static final String BORDER_LINE = "==========================================================";

    @Autowired
    private EntityManager entityManger;

    @Autowired
    private PlatformTransactionManager txManager;

    @Test
    void whenIdentityIdStrategyInsert_thenExecutedBeforeCommitButRollBack() {
        TransactionTemplate tx = new TransactionTemplate(txManager);

        Long generatedId = tx.execute(status -> {
            // given
            Product p = new Product("p1", 10, 1000);

            // when
            System.out.println(BORDER_LINE);
            entityManger.persist(p);
            System.out.println(BORDER_LINE);

            // then
            if(p.getId() == null) {
                throw new AssertionError("IDENTITY인데 persist 직후 id가 null이면 flush 타이밍/설정 확인 필요");
            }

            status.setRollbackOnly();
            return p.getId();
        });

        tx.executeWithoutResult(status -> {
            entityManger.clear();
            Product found = entityManger.find(Product.class, generatedId);
            if(found != null) {
                throw new AssertionError("롤백 했는데도 데이터가 남아 있습니다.");
            }
        });

    }
}
