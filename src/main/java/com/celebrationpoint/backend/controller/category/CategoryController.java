package com.celebrationpoint.backend.controller.category;

import com.celebrationpoint.backend.entity.Category;
import com.celebrationpoint.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // ✅ GET ALL ACTIVE CATEGORIES
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(
                categoryRepository.findAll()
                        .stream()
                        .filter(Category::isActive)
                        .toList()
        );
    }
}
