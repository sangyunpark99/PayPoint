package org.sangyunpark.pointpay.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.product.controller.dto.request.ProductCreateRequest;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductCreateResponse;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductCursorPageResponse;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductResponse;
import org.sangyunpark.pointpay.domain.product.service.ProductService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ProductCreateResponse createProduct(@RequestBody ProductCreateRequest request) {
        return productService.createProduct(request.getName(), request.getStock());
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable long productId) {
        return productService.getProduct(productId);
    }

    @GetMapping
    public ProductCursorPageResponse getProducts(
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorCreatedAt,
            @RequestParam(required = false) Long cursorId
    ) {
        return productService.getProductByCursor(size, cursorCreatedAt, cursorId);
    }
}
