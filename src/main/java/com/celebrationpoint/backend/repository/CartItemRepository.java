package com.celebrationpoint.backend.repository;

import com.celebrationpoint.backend.entity.Cart;
import com.celebrationpoint.backend.entity.CartItem;
import com.celebrationpoint.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    void deleteByCart(Cart cart);
}
