package com.celebrationpoint.backend.controller.admin;

import com.celebrationpoint.backend.entity.Role;
import com.celebrationpoint.backend.constants.RoleType;
import com.celebrationpoint.backend.entity.User;
import com.celebrationpoint.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

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
    private PaymentRepository paymentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // ===============================
    // ✅ GET ALL USERS (ADMIN)
    // ===============================
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            // Filter out admin users and format the response
            List<Map<String, Object>> response = users.stream()
                    .filter(user -> !hasAdminRole(user)) // Exclude admins
                    .map(this::userToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to fetch users", "message", e.getMessage()));
        }
    }

    // ===============================
    // ✅ GET USER BY ID
    // ===============================
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            
            if (user.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "User not found"));
            }
            
            if (hasAdminRole(user.get())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Cannot access admin user details"));
            }
            
            return ResponseEntity.ok(userToMap(user.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to fetch user", "message", e.getMessage()));
        }
    }

    // ===============================
    // ✅ DELETE USER
    // ===============================
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            
            if (user.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "User not found"));
            }
            
            User userToDelete = user.get();
            
            // Prevent deleting admin users
            if (hasAdminRole(userToDelete)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Cannot delete admin users"));
            }
            
            // Delete related records in order: Orders → OrderItems, Payments → CartItems → Carts → AuditLogs → User
            
            // 1. Delete OrderItems associated with user's orders
            var userOrders = orderRepository.findByUser(userToDelete);
            for (var order : userOrders) {
                orderItemRepository.deleteByOrder(order);
            }
            
            // 2. Delete Payments associated with user's orders
            for (var order : userOrders) {
                paymentRepository.deleteByOrder(order);
            }
            
            // 3. Delete all orders for this user
            orderRepository.deleteByUser(userToDelete);
            
            // 4. Delete CartItems for user's cart
            var userCart = cartRepository.findByUser(userToDelete);
            if (userCart.isPresent()) {
                cartItemRepository.deleteByCart(userCart.get());
            }
            
            // 5. Delete user's cart
            cartRepository.deleteByUser(userToDelete);
            
            // 6. Delete audit logs for this user
            auditLogRepository.deleteByPerformedBy(userToDelete.getEmail());
            
            // 7. Finally delete the user
            userRepository.deleteById(id);
            
            return ResponseEntity.ok(Map.of(
                    "message", "User deleted successfully",
                    "userId", id
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to delete user", "message", e.getMessage()));
        }
    }

    // ===============================
    // ✅ TOGGLE USER STATUS (Block/Unblock)
    // ===============================
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Optional<User> user = userRepository.findById(id);
            
            if (user.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "User not found"));
            }
            
            User userToUpdate = user.get();
            
            // Prevent blocking admin users
            if (hasAdminRole(userToUpdate)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Cannot block admin users"));
            }
            
            // Get the blocked status from request
            Object blockedObj = request.get("blocked");
            if (blockedObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid input: 'blocked' field is required"));
            }
            
            boolean blocked = (boolean) blockedObj;
            
            // Set enabled as opposite of blocked
            userToUpdate.setEnabled(!blocked);
            userRepository.save(userToUpdate);
            
            return ResponseEntity.ok(Map.of(
                    "message", blocked ? "User blocked successfully" : "User unblocked successfully",
                    "user", userToMap(userToUpdate)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to update user status", "message", e.getMessage()));
        }
    }

    // ===============================
    // 🔧 HELPER METHODS
    // ===============================
    
    /**
     * Check if user has admin role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ROLE_ADMIN);
    }

    /**
     * Convert User entity to Map for API response
     */
    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("firstName", extractFirstName(user.getFullName()));
        map.put("lastName", extractLastName(user.getFullName()));
        map.put("fullName", user.getFullName());
        map.put("blocked", !user.isEnabled()); // Invert enabled to blocked
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("createdAt", new Date()); // Using current date as fallback
        
        return map;
    }

    /**
     * Extract first name from full name
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Extract last name from full name
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        }
        return "";
    }
}
