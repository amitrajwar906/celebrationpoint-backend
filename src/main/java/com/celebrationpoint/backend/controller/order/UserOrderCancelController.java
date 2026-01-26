package com.celebrationpoint.backend.controller.order;

import com.celebrationpoint.backend.repository.UserRepository;
import com.celebrationpoint.backend.service.order.OrderCancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class UserOrderCancelController {

    @Autowired
    private OrderCancelService orderCancelService;

    @Autowired
    private UserRepository userRepository;

    // ===============================
    // ❌ USER CANCEL ORDER
    // ===============================
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        orderCancelService.cancelOrderByUser(orderId, userId);

        return ResponseEntity.ok(
                Map.of("message", "Order cancelled successfully")
        );
    }

    // ===============================
    // 🔒 HELPER METHOD
    // ===============================
    private Long getUserId(Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
