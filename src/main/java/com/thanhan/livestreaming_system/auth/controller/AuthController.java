package com.thanhan.livestreaming_system.auth.controller;


import com.nimbusds.jose.JOSEException;
import com.thanhan.livestreaming_system.auth.dto.request.AuthenticationRequest;
import com.thanhan.livestreaming_system.auth.dto.request.IntrospectRequest;
import com.thanhan.livestreaming_system.auth.dto.request.LogoutRequest;
import com.thanhan.livestreaming_system.auth.dto.request.RefreshTokenRequest;
import com.thanhan.livestreaming_system.auth.dto.response.AuthenticationResponse;
import com.thanhan.livestreaming_system.auth.dto.response.IntrospectResponse;
import com.thanhan.livestreaming_system.auth.service.AuthenticationService;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class AuthController {

    AuthenticationService authenticationService;

    @PostMapping("/log-in")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> logIn(@RequestBody AuthenticationRequest request) throws JOSEException {
        var result = authenticationService.authenticate(request);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false) //Set secure = true khi gui bang HTTPS
                .path("/livestream/auth/")
                .maxAge(Duration.ofDays(5))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(ApiResponse.<AuthenticationResponse>builder()
                                .message("Successfully logged in")
                                        .data(result)
                                        .build());
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        log.info("Start introspect");
        return ApiResponse.<IntrospectResponse>builder()
                .data(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(@CookieValue String refreshToken) throws JOSEException {

        AuthenticationResponse result = authenticationService.refreshToken(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false) //Set secure = true khi gui bang HTTPS
                .path("/livestream/auth/")
                .maxAge(Duration.ofDays(5))
                .sameSite("Lax")
                .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.<AuthenticationResponse>builder()
                        .message("Refresh token successful!")
                        .data(result)
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request,
                                                    @CookieValue(value = "refreshToken", required = false) String refreshToken) throws ParseException, JOSEException {
        authenticationService.logout(request, refreshToken);

        ResponseCookie rmCookie = ResponseCookie.from("refreshToken", "")
                .path("/livestream/auth/")
                .maxAge(0)
                .httpOnly(true)
                .build();

        return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, rmCookie.toString())
                        .body(ApiResponse.<Void>builder()
                                        .message("Logout success")
                                        .build());
    }


}

