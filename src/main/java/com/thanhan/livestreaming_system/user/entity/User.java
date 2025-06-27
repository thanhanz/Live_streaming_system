package com.thanhan.livestreaming_system.user.entity;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;


import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NonNull
    private String username;

    @NonNull
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String description;

    @Nullable
    private String avatar;
}
