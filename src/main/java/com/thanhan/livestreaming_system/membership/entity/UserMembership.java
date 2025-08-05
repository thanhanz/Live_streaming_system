package com.thanhan.livestreaming_system.membership.entity;

import com.thanhan.livestreaming_system.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    User user;

    @ManyToOne
    MembershipPackage membershipPackage;

    private LocalDateTime createdAt;
    private LocalDateTime endAt;

}
