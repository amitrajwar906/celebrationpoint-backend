package com.celebrationpoint.backend.controller.admin;

import com.celebrationpoint.backend.entity.Category;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // =================================================
    // 📄 GET ALL CATEGORIES (ADMIN)
    // =================================================
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    // =================================================
    // ➕ ADD CATEGORY
    // =================================================
    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> request) {

        String name = request.get("name");
        String description = request.get("description");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Category name is required"));
        }

        if (categoryRepository.existsByName(name)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Category already exists"));
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setActive(true);

        categoryRepository.save(category);

        return ResponseEntity.ok(
                Map.of("message", "Category added successfully")
        );
    }

    // =================================================
    // ✏ UPDATE CATEGORY
    // =================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + id)
                );

        if (request.containsKey("name")) {
            String name = request.get("name");
            if (name != null && !name.isBlank()) {
                category.setName(name);
            }
        }

        if (request.containsKey("description")) {
            category.setDescription(request.get("description"));
        }

        categoryRepository.save(category);

        return ResponseEntity.ok(
                Map.of("message", "Category updated successfully")
        );
    }

    // =================================================
    // 🔁 ACTIVATE / DEACTIVATE CATEGORY
    // =================================================
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleCategoryStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + id)
                );

        category.setActive(active);
        categoryRepository.save(category);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        active ? "Category activated" : "Category deactivated"
                )
        );
    }

    // =================================================
    // ❌ DELETE CATEGORY
    // =================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + id)
                );

        categoryRepository.delete(category);

        return ResponseEntity.ok(
                Map.of("message", "Category deleted successfully")
        );
    }
}
