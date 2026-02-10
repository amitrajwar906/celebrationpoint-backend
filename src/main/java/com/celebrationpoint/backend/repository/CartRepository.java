package com.celebrationpoint.backend.repository;

import com.celebrationpoint.backend.entity.Cart;
import com.celebrationpoint.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    boolean existsByUser(User user);
    
    // ✅ Delete user's cart
    void deleteByUser(User user);
}
