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
    INVALID_MESSAGE_KEY(1012, "Message key for valid is wrong!"),
    USER_NOT_EXIST(4004, "User not exist"),
    UNAUTHENTICATED(4001, "Unauthenticated"),
    REVOKED_TOKEN(4003, "Revoked or expired token"),
    FORBIDDEN(403, "You do not have permission to access this resource"),
    USER_BANNED_CHAT(404, "You have banned this chat"),
    ;

    int code;
    String message;
}
