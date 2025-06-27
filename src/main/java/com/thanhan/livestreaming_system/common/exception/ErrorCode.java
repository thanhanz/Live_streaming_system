package com.thanhan.livestreaming_system.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(999, "Uncategorized Exception!"),
    USER_EXISTED(1001, "Username is existed!"),
    USERNAME_INVALID(1010, "Username must be at least 4 characters!"),
    PASSWORD_INVALID(1011, "Password must be at least 6 characters!"),
    INVALID_MESSAGE_KEY(1012, "Message key for valid is wrong!");
    ;

    int code;
    String message;
}
