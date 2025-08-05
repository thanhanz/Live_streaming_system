package com.thanhan.livestreaming_system.common.exception;

import lombok.Getter;

@Getter
public class VnpPaymentException extends RuntimeException {

    VnpErrorCode vnpErrorCode;

    public VnpPaymentException(VnpErrorCode vnpErrorCode) {
        super(vnpErrorCode.getMessage());
        this.vnpErrorCode = vnpErrorCode;
    }

}
