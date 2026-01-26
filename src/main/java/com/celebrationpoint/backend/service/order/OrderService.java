package com.celebrationpoint.backend.service.order;

import com.celebrationpoint.backend.entity.*;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.*;
import com.celebrationpoint.backend.service.audit.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AuditLogService auditLogService;

    // =================================================
    // 🛒 PLACE ORDER
    // =================================================
    @Transactional
    public Order placeOrder(
            Long userId,
            String fullName,
            String phone,
            String addressLine,
            String shippingAddress,
            String city,
            String state,
            String pincode
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);

        order.setFullName(fullName);
        order.setPhone(phone);
        order.setAddressLine(addressLine);
        order.setShippingAddress(shippingAddress);
        order.setCity(city);
        order.setState(state);
        order.setPostalCode(pincode);
        order.setPincode(pincode);  // Set both postalCode and pincode for database compatibility

        order = orderRepository.save(order);

        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem(
                    order,
                    item.getProduct(),
                    item.getProduct().getName(),
                    item.getPrice(),
                    item.getQuantity()
            );
            orderItemRepository.save(orderItem);
        }

        cartItemRepository.deleteByCart(cart);

        // 📝 AUDIT LOG
        auditLogService.logAction(
                "ORDER_CREATED",
                "ORDER",
                order.getId(),
                null,
                OrderStatus.PENDING.name(),
                user.getEmail(),
                "USER"
        );

        return order;
    }

    // =================================================
    // 📦 USER ORDER HISTORY
    // =================================================
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // =================================================
    // 🛠 ADMIN: UPDATE ORDER STATUS
    // =================================================
    @Transactional
    public Order updateOrderStatus(
            Long orderId,
            OrderStatus newStatus,
            String adminEmail
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String oldStatus = order.getStatus().name();

        order.setStatus(newStatus);
        orderRepository.save(order);

        // 📝 AUDIT LOG
        auditLogService.logAction(
                "ORDER_STATUS_CHANGED",
                "ORDER",
                order.getId(),
                oldStatus,
                newStatus.name(),
                adminEmail,
                "ADMIN"
        );

        return order;
    }

    // =================================================
    // ❌ USER: CANCEL ORDER
    // =================================================
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Order cannot be cancelled at this stage");
        }

        String oldStatus = order.getStatus().name();

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // 📝 AUDIT LOG
        auditLogService.logAction(
                "ORDER_CANCELLED",
                "ORDER",
                order.getId(),
                oldStatus,
                OrderStatus.CANCELLED.name(),
                order.getUser().getEmail(),
                "USER"
        );
    }
}
