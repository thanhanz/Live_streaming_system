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
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class AuthController {

    AuthenticationService authenticationService;

    @PostMapping("/log-in")
    public ApiResponse<AuthenticationResponse> logIn(@RequestBody AuthenticationRequest request) throws JOSEException {
        var result = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        log.info("Start introspect");
        return ApiResponse.<IntrospectResponse>builder()
                .data(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) throws JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Refresh Token successful")
                .data(authenticationService.refreshToken(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);

        return ApiResponse.<Void>builder()
                .message("Logout success")
                .build();
    }


}

