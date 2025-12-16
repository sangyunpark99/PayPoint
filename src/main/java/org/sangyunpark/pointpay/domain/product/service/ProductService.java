package org.sangyunpark.pointpay.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductCreateResponse;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductResponse;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.sangyunpark.pointpay.domain.product.repository.ProductJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductJpaRepository productJpaRepository;

    @Transactional
    public ProductCreateResponse createProduct(String name, int stock) {
        Product product = Product.builder().name(name).stock(stock).build();
        Product savedProduct = productJpaRepository.save(product);
        return ProductCreateResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(long productId) {
        Product findProduct = productJpaRepository.findById(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(findProduct);
    }
}
