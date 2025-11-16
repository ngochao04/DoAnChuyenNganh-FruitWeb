package Fruit.Web.controllers;

import Fruit.Web.models.Product;
import Fruit.Web.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shop")
public class ShopApiController {

    private final ProductRepository productRepository;

    public ShopApiController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * API công khai để lấy danh sách sản phẩm cho trang shop
     * Hỗ trợ: tìm kiếm, lọc theo danh mục, phân trang
     */
    @GetMapping("/products")
    public Page<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        // ✅ LOGIC LỌC SẢN PHẨM
        if (categoryId != null && q != null && !q.trim().isEmpty()) {
            // Có cả categoryId và keyword
            productPage = productRepository.findByCategoryIdAndKeyword(categoryId, q, pageable);
        } else if (categoryId != null) {
            // Chỉ có categoryId
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else if (q != null && !q.trim().isEmpty()) {
            // Chỉ có keyword
            productPage = productRepository.findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(q, q, pageable);
        } else {
            // Lấy tất cả
            productPage = productRepository.findAll(pageable);
        }

        // Map sang JSON, chỉ lấy sản phẩm active
        return productPage
                .map(this::mapProductToJson);
    }

    /**
     * Map Product entity sang JSON response
     */
    private Map<String, Object> mapProductToJson(Product p) {
        // Chỉ trả về sản phẩm đang active
        if (p.getActive() == null || !p.getActive()) {
            return null;
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("slug", p.getSlug());
        m.put("brand", p.getBrand());
        m.put("origin", p.getOrigin());
        m.put("shortDesc", p.getShortDesc());
        m.put("mainImageUrl", p.getMainImageUrl());
        m.put("active", p.getActive());

        // Giá hiển thị
        m.put("basePrice", p.getBasePrice());
        m.put("priceMin", p.getPriceMin());
        m.put("priceMax", p.getPriceMax());
        m.put("stockTotal", p.getStockTotal());

        // Danh mục
        List<Map<String, Object>> categories = p.getCategories().stream()
                .map(c -> {
                    Map<String, Object> catMap = new LinkedHashMap<>();
                    catMap.put("id", c.getId());
                    catMap.put("name", c.getName());
                    catMap.put("slug", c.getSlug());
                    return catMap;
                })
                .collect(Collectors.toList());
        m.put("categories", categories);

        return m;
    }

    /**
     * API lấy chi tiết 1 sản phẩm
     */
    @GetMapping("/products/{id}")
    public Map<String, Object> getProductDetail(@PathVariable Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!p.getActive()) {
            throw new RuntimeException("Product is not available");
        }

        return mapProductToJson(p);
    }
}