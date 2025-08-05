package com.thanhan.livestreaming_system.membership.entity;

import com.thanhan.livestreaming_system.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String gateway;

    @Column(name = "transaction_id", unique = true) // Thường nên unique
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "membership_package_id")
    MembershipPackage membershipPackage;
}
