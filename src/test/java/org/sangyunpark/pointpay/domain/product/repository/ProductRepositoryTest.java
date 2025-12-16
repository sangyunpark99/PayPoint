package org.sangyunpark.pointpay.domain.product.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.RepositoryTest;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductRepositoryTest extends RepositoryTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    @Transactional
    @DisplayName("findByAll(ids)는 ids 입력 순서를 보장하지 않습니다.")
    void whenFindByAllIds_thenNotGuaranteeOrder() {
        // given
        Product p1 = productJpaRepository.save(new Product("p1", 1, 1000));
        Product p2 = productJpaRepository.save(new Product("p2", 1, 1000));
        Product p3 = productJpaRepository.save(new Product("p3", 1, 1000));

        List<Long> ids = List.of(p3.getId(), p1.getId(), p2.getId()); // 일부러 섞음

        // when
        List<Product> found = productJpaRepository.findAllById(ids);

        // then
        assertThat(found).hasSize(3);
        assertThat(found).extracting(Product::getId)
                .containsExactlyInAnyOrder(p1.getId(), p2.getId(), p3.getId());

        // 입력 순서는 동일하지 않습니다.
        System.out.println("input ids  = " + ids);
        System.out.println("found ids  = " + found.stream().map(Product::getId).toList());
    }
}
