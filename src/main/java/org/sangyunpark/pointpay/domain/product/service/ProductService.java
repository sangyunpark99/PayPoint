package org.sangyunpark.pointpay.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductCreateResponse;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductCursorPageResponse;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductResponse;
import org.sangyunpark.pointpay.domain.product.entity.Product;
import org.sangyunpark.pointpay.domain.product.repository.ProductJpaRepository;
import org.sangyunpark.pointpay.exception.BusinessException;
import org.sangyunpark.pointpay.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    public ProductCursorPageResponse getProductByCursor(int size, LocalDateTime cursorCreatedAt, Long cursorId) {
        List<Product> rows = productJpaRepository.findNextPage(
                cursorCreatedAt,
                cursorId,
                PageRequest.of(0, size + 1) // hasNext 확인용으로 1개 더 가져오기
        );

        boolean hasNext = rows.size() > size;
        if(hasNext) rows = rows.subList(0, size);

        List<ProductResponse> items = rows.stream()
                .map(ProductResponse::from)
                .toList();

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;

        if(hasNext && !rows.isEmpty()) {
            Product last = rows.get(rows.size() - 1);
            nextCreatedAt = last.getCreatedAt();
            nextId = last.getId();
        }

        return new ProductCursorPageResponse(items, hasNext, nextCreatedAt, nextId);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(long productId) {
        Product findProduct = productJpaRepository.findById(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(findProduct);
    }
}
