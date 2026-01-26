package com.celebrationpoint.backend.repository;

import com.celebrationpoint.backend.constants.RoleType;
import com.celebrationpoint.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);

    boolean existsByName(RoleType name);
}
