package com.thanhan.livestreaming_system.user.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Table(name = "follows")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

    private Instant followedAt;

}
