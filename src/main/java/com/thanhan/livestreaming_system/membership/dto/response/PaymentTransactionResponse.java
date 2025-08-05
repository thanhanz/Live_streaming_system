package com.thanhan.livestreaming_system.membership.dto.response;

public record PaymentTransactionResponse(
        String gateway,
        String transactionId,
        String status,
        String userId,
        String membershipPackageId) {
}
