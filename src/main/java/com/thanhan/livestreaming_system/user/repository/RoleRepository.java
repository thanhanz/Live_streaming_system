package com.thanhan.livestreaming_system.user.repository;

import com.thanhan.livestreaming_system.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
}
