package com.thanhan.livestreaming_system.user.repository;

import com.thanhan.livestreaming_system.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);


    @Query("SELECT u from User u where u.id = :id")
    Optional<User> findByStringId (@Param("id") UUID id);

    boolean existsByEmail(String email);


    @Query(value = "SELECT u from User u WHERE u.username LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByUsername(@Param("query")@NonNull String query);

    @Query(value = "SELECT COUNT(u.id) FROM users u JOIN users_roles ur ON ur.user_id = u.id " +
            " WHERE ur.roles_name='USER' ", nativeQuery = true)
    Integer countUsersByRolesName();

}
