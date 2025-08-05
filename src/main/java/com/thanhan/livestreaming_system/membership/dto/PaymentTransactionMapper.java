package com.thanhan.livestreaming_system.membership.dto;

import com.thanhan.livestreaming_system.membership.dto.response.PaymentTransactionResponse;
import com.thanhan.livestreaming_system.membership.entity.PaymentTransaction;

public class PaymentTransactionMapper {

    public static PaymentTransactionResponse toResponse(PaymentTransaction transaction) {
        return new PaymentTransactionResponse(
                transaction.getGateway(),
                transaction.getTransactionId(),
                transaction.getStatus().name(),
                transaction.getUser().getId().toString(),
                transaction.getMembershipPackage().getId().toString()
        );
    }
}
