package com.thanhan.livestreaming_system.membership.repository;

import com.thanhan.livestreaming_system.membership.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    @Query(value = "SELECT * FROM payment_transactions p WHERE p.transaction_id := transactionId", nativeQuery = true)
    PaymentTransaction getTransactionByTransactionId(@Param("transactionId") String transactionId);
}
