package com.thanhan.livestreaming_system.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public enum VnpErrorCode {

    SIGNATURE_FAILED(97, "Signature failed"),
    TRANSACTION_NOT_FOUND(01, "Transaction not found"),
    UNKNOWN_ERROR(99, "Unidentified error")
    ;

    int code;
    String message;
}
