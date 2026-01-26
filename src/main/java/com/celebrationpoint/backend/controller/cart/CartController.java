package com.celebrationpoint.backend.controller.cart;

import com.celebrationpoint.backend.dto.AddToCartRequest;
import com.celebrationpoint.backend.service.cart.CartService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ===============================
    // ➕ ADD TO CART
    // ===============================
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            Authentication authentication,
            @RequestBody AddToCartRequest request
    ) {
        String email = authentication.getName();

        cartService.addToCart(
                email,
                request.getProductId(),
                request.getQuantity()
        );

        return ResponseEntity.ok(
                Map.of("message", "Product added to cart")
        );
    }

    // ===============================
    // 👀 VIEW CART
    // ===============================
    @GetMapping
    public ResponseEntity<?> viewCart(Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.getCartItems(email)
        );
    }

    // ===============================
    // 🔄 UPDATE QUANTITY
    // ===============================
    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request
    ) {
        int quantity = request.get("quantity");

        cartService.updateQuantity(cartItemId, quantity);

        return ResponseEntity.ok(
                Map.of("message", "Cart item updated")
        );
    }

    // ===============================
    // ❌ REMOVE ITEM
    // ===============================
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long cartItemId) {

        cartService.removeItem(cartItemId);

        return ResponseEntity.ok(
                Map.of("message", "Cart item removed")
        );
    }

    // ===============================
    // 🗑️ CLEAR ENTIRE CART
    // ===============================
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication authentication) {

        String email = authentication.getName();

        cartService.clearCart(email);

        return ResponseEntity.ok(
                Map.of("message", "Cart cleared successfully")
        );
    }
}
