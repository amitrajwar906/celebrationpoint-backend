package com.celebrationpoint.backend.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.celebrationpoint.backend.repository.OrderRepository;
import com.celebrationpoint.backend.repository.PaymentRepository;
import com.celebrationpoint.backend.repository.UserRepository;
import com.celebrationpoint.backend.repository.ProductRepository;
import com.celebrationpoint.backend.repository.CategoryRepository;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminDashboardController(
            OrderRepository orderRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDashboardStats() {

        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();

        Double revenue = paymentRepository.getTotalSuccessfulRevenue();
        if (revenue == null) revenue = 0.0;

        return ResponseEntity.ok(
                Map.of(
                        "totalOrders", totalOrders,
                        "totalUsers", totalUsers,
                        "totalRevenue", revenue,
                        "totalProducts", totalProducts,
                        "totalCategories", totalCategories
                )
        );
    }
}
