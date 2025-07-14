package com.thanhan.livestreaming_system.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.thanhan.livestreaming_system.auth.dto.request.AuthenticationRequest;
import com.thanhan.livestreaming_system.auth.dto.request.IntrospectRequest;
import com.thanhan.livestreaming_system.auth.dto.request.LogoutRequest;
import com.thanhan.livestreaming_system.auth.dto.request.RefreshTokenRequest;
import com.thanhan.livestreaming_system.auth.dto.response.AuthenticationResponse;
import com.thanhan.livestreaming_system.auth.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {

    AuthenticationResponse authenticate(AuthenticationRequest request) throws JOSEException;
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
//    SignedJWT verifyToken(String token) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(String refreshToken) throws JOSEException;
    void logout(LogoutRequest request, String refreshToken) throws JOSEException, ParseException;
}
