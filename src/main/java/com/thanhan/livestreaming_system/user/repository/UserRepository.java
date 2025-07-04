package com.thanhan.livestreaming_system.user.repository;

import com.thanhan.livestreaming_system.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

    @Query("SELECT u from User u where u.id = :id")
    Optional<User> findByStringId (@Param("id") UUID id);
}
