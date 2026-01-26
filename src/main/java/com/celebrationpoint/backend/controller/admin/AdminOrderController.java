package com.celebrationpoint.backend.controller.admin;

import com.celebrationpoint.backend.entity.Order;
import com.celebrationpoint.backend.entity.OrderStatus;
import com.celebrationpoint.backend.entity.PaymentMethod;
import com.celebrationpoint.backend.entity.PaymentStatus;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.OrderRepository;
import com.celebrationpoint.backend.repository.OrderItemRepository;
import com.celebrationpoint.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // ==========================================
    // 📦 GET ALL ORDERS (ADMIN)
    // ==========================================
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // ==========================================
    // 🔍 GET ORDER BY ID
    // ==========================================
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return ResponseEntity.ok(order);
    }

    // ==========================================
    // 📦 GET ORDER ITEMS (ADMIN)
    // ==========================================
    @GetMapping("/{orderId}/items")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderItems(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

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

    // ==========================================
    // 🚚 UPDATE ORDER DELIVERY STATUS
    // ==========================================
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        OrderStatus newStatus = OrderStatus.valueOf(request.get("status"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(newStatus);
        orderRepository.save(order);

        // ===============================
        // 💵 COD → MARK PAID ON DELIVERY
        // ===============================
        if (newStatus == OrderStatus.DELIVERED) {

            paymentRepository.findByOrder(order).ifPresent(payment -> {
                if (payment.getMethod() == PaymentMethod.COD) {
                    payment.setStatus(PaymentStatus.COD_PAID);
                    paymentRepository.save(payment);
                }
            });
        }

        return ResponseEntity.ok(
                Map.of(
                        "orderId", orderId,
                        "newStatus", newStatus,
                        "message", "Order status updated"));
    }

    // ==========================================
    // 📋 GET COMPLETE ORDER DETAILS
    // ==========================================
    @GetMapping("/{orderId}/details")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        var payment = paymentRepository.findByOrder(order).orElse(null);

        Map<String, Object> orderDetails = Map.of(
                "orderId", order.getId(),
                "user", Map.of(
                        "userId", order.getUser().getId(),
                        "name", order.getUser().getFullName(), // Assuming getFullName() is the correct method
                        "email", order.getUser().getEmail(),
                        "phoneNumber", order.getUser().getPhoneNumber() // Assuming this method is now defined
                ),
                "orderStatus", order.getStatus(),
                "totalAmount", order.getTotalAmount(),
                "items", order.getOrderItems(),
                "shippingAddress", Map.of(
                        "address", order.getShippingAddress(),
                        "city", order.getCity(),
                        "state", order.getState(),
                        "postalCode", order.getPostalCode()
                ),
                "payment", payment != null ? Map.of(
                        "paymentId", payment.getId(),
                        "method", payment.getMethod(),
                        "status", payment.getStatus(),
                        "amount", payment.getAmount(),
                        "transactionId", payment.getTransactionId()
                ) : null,
                "createdAt", order.getCreatedAt(),
                "updatedAt", order.getUpdatedAt()
        );

        return ResponseEntity.ok(orderDetails);
    }
}
