package com.celebrationpoint.backend.controller.order;

import com.celebrationpoint.backend.entity.Order;
import com.celebrationpoint.backend.entity.User;
import com.celebrationpoint.backend.repository.OrderItemRepository;
import com.celebrationpoint.backend.repository.UserRepository;
import com.celebrationpoint.backend.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

        @Autowired
        private OrderService orderService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private OrderItemRepository orderItemRepository;

        // ===============================
        // 🔒 HELPER METHOD
        // ===============================
        private Long getUserId(Authentication authentication) {

                String email = authentication.getName();

                return userRepository.findByEmail(email)
                                .map(User::getId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Invalid authentication token"));
        }

        // ===============================
        // 📦 GET MY ORDERS
        // ===============================
        @GetMapping
        @Transactional(readOnly = true)
        public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {

                Long userId = getUserId(authentication);

                List<Order> orders = orderService.getOrdersByUser(userId);

                return ResponseEntity.ok(orders);
        }

        @GetMapping("/{orderId}/items")
        @Transactional(readOnly = true)
        public ResponseEntity<?> getOrderItems(
                        @PathVariable Long orderId,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                Order order = orderService.getOrdersByUser(userId).stream()
                                .filter(o -> o.getId().equals(orderId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                var items = orderItemRepository.findByOrder(order)
                                .stream()
                                .map(item -> new com.celebrationpoint.backend.dto.OrderItemResponse(
                                                item.getProduct().getId(),
                                                item.getProductName(),
                                                item.getPrice(),
                                                item.getQuantity()))
                                .toList();

                return ResponseEntity.ok(items);
        }

}
