package com.thanhan.livestreaming_system.membership.entity;

import com.thanhan.livestreaming_system.user.entity.Channel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private int price;

    private int duration; //Day

    @ManyToOne
    Channel channel;
}
