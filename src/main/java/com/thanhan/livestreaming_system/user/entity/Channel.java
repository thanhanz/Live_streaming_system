package com.thanhan.livestreaming_system.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Table(name = "channels")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    private String displayName;
    private String description;
    private String avatarUrl;
    private String bannerUrl;
    private Instant createdAt;
    private Instant updatedAt;

    @Column(nullable = false, unique = true)
    private String streamKey;

}
