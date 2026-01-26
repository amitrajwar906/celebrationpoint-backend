package com.celebrationpoint.backend.controller.admin;

import com.celebrationpoint.backend.entity.Category;
import com.celebrationpoint.backend.entity.Product;
import com.celebrationpoint.backend.repository.CategoryRepository;
import com.celebrationpoint.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ===============================
    // ✅ GET ALL PRODUCTS (ADMIN)
    // ===============================
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ===============================
    // ✅ CREATE PRODUCT
    // ===============================
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Double price = ((Number) request.get("price")).doubleValue();
            Integer stockQuantity = ((Number) request.get("stockQuantity")).intValue();
            Long categoryId = ((Number) request.get("categoryId")).longValue();

            // Validation
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Product name is required"));
            }

            if (price == null || price <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Price must be greater than 0"));
            }

            if (stockQuantity == null || stockQuantity < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Stock quantity cannot be negative"));
            }

            // Get category
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            // Create product
            Product product = new Product();
            product.setName(name.trim());
            product.setDescription(description != null ? description.trim() : "");
            product.setPrice(new java.math.BigDecimal(price));
            product.setStockQuantity(stockQuantity);
            product.setCategory(category);
            String imageUrl = (String) request.get("imageUrl");
            product.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
            product.setActive(true);

            Product savedProduct = productRepository.save(product);

            return ResponseEntity.ok(Map.of(
                    "message", "Product created successfully",
                    "product", savedProduct
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ===============================
    // ✅ UPDATE PRODUCT
    // ===============================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Update fields
            if (request.containsKey("name")) {
                String name = (String) request.get("name");
                if (name != null && !name.trim().isEmpty()) {
                    product.setName(name.trim());
                }
            }

            if (request.containsKey("description")) {
                String description = (String) request.get("description");
                product.setDescription(description != null ? description.trim() : "");
            }

            if (request.containsKey("price")) {
                Double price = ((Number) request.get("price")).doubleValue();
                if (price > 0) {
                    product.setPrice(new java.math.BigDecimal(price));
                }
            }

            if (request.containsKey("stockQuantity")) {
                Integer stockQuantity = ((Number) request.get("stockQuantity")).intValue();
                if (stockQuantity >= 0) {
                    product.setStockQuantity(stockQuantity);
                }
            }

            if (request.containsKey("categoryId")) {
                Long categoryId = ((Number) request.get("categoryId")).longValue();
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                product.setCategory(category);
            }

            if (request.containsKey("imageUrl")) {
                String imageUrl = (String) request.get("imageUrl");
                product.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
            }

            if (request.containsKey("active")) {
                Boolean active = (Boolean) request.get("active");
                product.setActive(active != null ? active : true);
            }

            Product updatedProduct = productRepository.save(product);

            return ResponseEntity.ok(Map.of(
                    "message", "Product updated successfully",
                    "product", updatedProduct
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ===============================
    // ✅ DELETE PRODUCT
    // ===============================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            productRepository.delete(product);

            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ===============================
    // ✅ TOGGLE PRODUCT STATUS
    // ===============================
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleProductStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setActive(active);
            productRepository.save(product);

            return ResponseEntity.ok(Map.of(
                    "message", active ? "Product activated" : "Product deactivated"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ===============================
    // ✅ GET PRODUCT BY ID
    // ===============================
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
