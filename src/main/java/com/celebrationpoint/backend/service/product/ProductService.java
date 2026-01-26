package com.celebrationpoint.backend.service.product;

import com.celebrationpoint.backend.entity.Category;
import com.celebrationpoint.backend.entity.Product;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.CategoryRepository;
import com.celebrationpoint.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ✅ CREATE PRODUCT
    public Product createProduct(
            String name,
            String description,
            BigDecimal price,
            int stockQuantity,
            Long categoryId
    ) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + categoryId)
                );

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setActive(true);

        return productRepository.save(product);
    }

    // ✅ GET ALL ACTIVE PRODUCTS
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    // ✅ GET PRODUCTS BY CATEGORY
    public List<Product> getProductsByCategory(Long categoryId) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }

        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }

    // ✅ GET PRODUCT BY ID
    public Product getProductById(Long productId) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + productId)
                );
    }

    // ✅ SOFT DELETE PRODUCT
    public void disableProduct(Long productId) {

        Product product = getProductById(productId);
        product.setActive(false);
        productRepository.save(product);
    }
}
