package com.thanhan.livestreaming_system.user.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

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
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

    @CreatedDate
    private Instant followedAt;
}
