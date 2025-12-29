package org.sangyunpark.pointpay.domain.product.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.sangyunpark.pointpay.domain.product.repository.ProductJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ProductServiceTest extends CommonTest {

    @Autowired // 필드 주입 - 객체 생성 이후 리플렉션으로 값을 넣는 방식
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private EntityManager em;

    @Test
    @Transactional
    @DisplayName("상품을 추가하는 경우 조회가 됩니다.")
    public void createProduct() {
        // given
        Product product = Product.builder()
                .name("test")
                .price(1000)
                .stock(10)
                .build();

        Product savedProduct = productJpaRepository.save(product);

        // when
        Product findProduct = productJpaRepository.findById(savedProduct.getId()).get();

        // then
        assertThat(savedProduct.getId()).isEqualTo(findProduct.getId());
        assertThat(savedProduct.getName()).isEqualTo(findProduct.getName());
        assertThat(savedProduct.getStock()).isEqualTo(findProduct.getStock());
    }

    @Test
    @Transactional
    @DisplayName("findAllById 후, HashMap으로 변환하면 순서 보장을 할 수 있습니다.")
    void whenConvertHashMap_thenGuaranteeOrder() {

        Product p1 = productJpaRepository.save(new Product("test_1", 0, 0));
        Product p2 = productJpaRepository.save(new Product("test_2", 0, 0));

        List<Long> ids = List.of(p1.getId(), p2.getId());

        em.clear();

        List<Product> products = productJpaRepository.findAllById(ids);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        assertThat(productMap).isInstanceOf(HashMap.class);
        assertThat(productMap).isNotInstanceOf(TreeMap.class);
        assertThat(productMap).isNotInstanceOf(ConcurrentHashMap.class);

        assertThat(productMap.get(p1.getId()).getName()).isEqualTo(p1.getName());
        assertThat(productMap.get(p2.getId()).getName()).isEqualTo(p2.getName());
    }

    @Test
    @Transactional
    @DisplayName("동일한 아이디를 가진 상품 목록을 HashMap으로 변환시 IllegalException이 발생합니다.")
    void whenProductIdsDuplicated_thenThrowIllegalException() throws Exception {

        Product p1 = new Product("test_1", 0, 0);
        Product p2 = new Product("test_2", 0, 0);

        setId(p1, 1L);
        setId(p2, 1L);

        List<Product> products = List.of(p1, p2);

        assertThatThrownBy(() -> products.stream().collect(Collectors.toMap(Product::getId, Function.identity())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @Transactional
    @DisplayName("동일한 시간 상품 조회시, id 순서대로 조회됩니다.")
    void whenCreateProductSameTime_then() {
        Product a = productJpaRepository.save(new Product("a", 1, 100));
        Product b = productJpaRepository.save(new Product("b", 1, 100));
        Product c = productJpaRepository.save(new Product("c", 1, 100));

        em.createNativeQuery("""
        UPDATE products
        SET created_at = '2025-12-17 10:00:00'
        WHERE id IN (:a, :b, :c)
        """)
                .setParameter("a", a.getId())
                .setParameter("b", b.getId())
                .setParameter("c", c.getId())
                .executeUpdate();

        em.clear();

        List<Product> page = productJpaRepository.findAll(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        assertThat(page.stream().map(Product::getId).toList())
                .containsSubsequence(
                        List.of(a.getId(), b.getId(), c.getId()));
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field f = entity.getClass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity,id);
    }

    @AfterEach
    void clean() {
        dbCleaner.clear();
    }
}