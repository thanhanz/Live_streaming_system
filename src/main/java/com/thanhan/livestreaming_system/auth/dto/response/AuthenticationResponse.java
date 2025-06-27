package com.thanhan.livestreaming_system.auth.dto.response;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AuthenticationResponse {
    String accessToken;
    String refreshToken;
    boolean isAuthenticated;


}
