package org.sangyunpark.pointpay.domain.product.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.sangyunpark.pointpay.domain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

class ProductServiceTest extends CommonTest {

    @Autowired // 필드 주입 - 객체 생성 이후 리플렉션으로 값을 넣는 방식
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품을 추가하는 경우 조회가 됩니다.")
    public void createProduct() {
        // given
        Product product = Product.builder()
                .name("test")
                .price(1000)
                .stock(10)
                .build();

        Product savedProduct = productRepository.save(product);

        // when
        Product findProduct = productRepository.findById(savedProduct.getId()).get();

        // then
        Assertions.assertThat(savedProduct.getId()).isEqualTo(findProduct.getId());
        Assertions.assertThat(savedProduct.getName()).isEqualTo(findProduct.getName());
        Assertions.assertThat(savedProduct.getStock()).isEqualTo(findProduct.getStock());
    }
}