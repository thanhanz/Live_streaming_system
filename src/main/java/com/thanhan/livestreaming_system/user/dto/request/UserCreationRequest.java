package com.thanhan.livestreaming_system.user.dto.request;

import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import jakarta.validation.constraints.Size;

public record UserCreationRequest (
        String firstName,
        String lastName,
        String email,
        @Size(min = 4, message = "USERNAME_INVALID")
        String username,
        @Size(min = 6, message = "PASSWORD_INVALID")
        String password) {

}
