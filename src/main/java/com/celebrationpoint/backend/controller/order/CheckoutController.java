package com.celebrationpoint.backend.controller.order;

import com.celebrationpoint.backend.entity.Order;
import com.celebrationpoint.backend.repository.UserRepository;
import com.celebrationpoint.backend.service.order.OrderService;
import com.celebrationpoint.backend.service.payment.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/checkout")
@CrossOrigin
public class CheckoutController {

        @Autowired
        private PaymentService paymentService;

        @Autowired
        private OrderService orderService;

        @Autowired
        private UserRepository userRepository;

        // ✅ PLACE ORDER (FROM CART)
       
        @PostMapping
        public ResponseEntity<?> checkout(
                        @RequestBody Map<String, String> request,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                String paymentMethod = request.get("paymentMethod"); // ONLINE or COD

                Order order = orderService.placeOrder(
                                userId,
                                request.get("fullName"),
                                request.get("phone"),
                                request.get("addressLine"),
                                request.get("shippingAddress"),
                                request.get("city"),
                                request.get("state"),
                                request.get("postalCode"));

                // ===============================
                // 💵 CASH ON DELIVERY FLOW
                // ===============================
                if ("COD".equalsIgnoreCase(paymentMethod)) {

                        paymentService.createCodPayment(order);

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "orderId", order.getId(),
                                                        "status", "CONFIRMED",
                                                        "paymentMethod", "COD",
                                                        "message", "Order placed with Cash On Delivery"));
                }

                // ===============================
                // 💳 ONLINE PAYMENT FLOW
                // ===============================
                return ResponseEntity.ok(
                                Map.of(
                                                "orderId", order.getId(),
                                                "status", "PENDING",
                                                "paymentMethod", "ONLINE",
                                                "message", "Proceed to online payment"));
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
