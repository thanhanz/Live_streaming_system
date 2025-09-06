package com.thanhan.livestreaming_system.membership.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thanhan.livestreaming_system.membership.dto.request.StatisticsMembershipGet;
import com.thanhan.livestreaming_system.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime endAt;

}
