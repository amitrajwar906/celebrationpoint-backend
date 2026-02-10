package com.celebrationpoint.backend.service.cart;

import com.celebrationpoint.backend.dto.CartItemResponse;
import com.celebrationpoint.backend.entity.Cart;
import com.celebrationpoint.backend.entity.CartItem;
import com.celebrationpoint.backend.entity.Product;
import com.celebrationpoint.backend.entity.User;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.CartItemRepository;
import com.celebrationpoint.backend.repository.CartRepository;
import com.celebrationpoint.backend.repository.ProductRepository;
import com.celebrationpoint.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ===============================
    // 🧠 CORE: GET OR CREATE CART
    // ===============================
    private Cart getOrCreateCart(User user) {
        try {
            // First try to find existing cart
            var existingCart = cartRepository.findByUser(user);
            if (existingCart.isPresent()) {
                return existingCart.get();
            }
            
            // If cart doesn't exist, create new one
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        } catch (Exception e) {
            // If creation fails, try to find the cart again (might have been created by another thread)
            return cartRepository.findByUser(user)
                    .orElse(null);
        }
    }

    // ===============================
    // ➕ ADD PRODUCT TO CART
    // ===============================
    public void addToCart(String email, Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Cart cart = getOrCreateCart(user);
        
        if (cart == null) {
            throw new RuntimeException("Failed to create or retrieve cart for user: " + email);
        }

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem(
                    cart,
                    product,
                    quantity,
                    product.getPrice() // snapshot price
            );
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        cartItemRepository.save(cartItem);
    }

    // ===============================
    // 👀 VIEW CART ITEMS
    // ===============================
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Don't auto-create cart on view - just return items from existing cart
        // Use Optional to avoid creating cart automatically
        Cart cart = cartRepository.findByUser(user).orElse(null);
        
        if (cart == null) {
            // Return empty list if no cart exists yet
            return List.of();
        }

        return cartItemRepository.findByCart(cart).stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getPrice(),
                        item.getQuantity()
                ))
                .toList();
    }

    // ===============================
    // 🔄 UPDATE QUANTITY
    // ===============================
    public void updateQuantity(Long cartItemId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    // ===============================
    // ❌ REMOVE ITEM
    // ===============================
    public void removeItem(Long cartItemId) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItemRepository.delete(item);
    }

    // ===============================
    // 🧹 CLEAR CART (AFTER ORDER)
    // ===============================
    public void clearCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCart(cart);
    }
}
