package org.sangyunpark.pointpay.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.product.controller.dto.request.ProductCreateRequest;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductCreateResponse;
import org.sangyunpark.pointpay.domain.product.controller.dto.response.ProductResponse;
import org.sangyunpark.pointpay.domain.product.service.ProductService;
import org.springframework.web.bind.annotation.*;

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
}
