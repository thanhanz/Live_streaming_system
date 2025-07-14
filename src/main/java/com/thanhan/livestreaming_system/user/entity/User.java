package com.thanhan.livestreaming_system.user.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.UuidGenerator;


import java.time.Instant;
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
    private Instant createdAt;
    private Instant updatedAt;
    @Nullable
    private String avatar;

    @OneToOne(mappedBy = "owner",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Channel channel;

}
