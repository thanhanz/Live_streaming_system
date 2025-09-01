package com.thanhan.livestreaming_system.auth.service.impl;

import com.thanhan.livestreaming_system.auth.service.JwtService;
import com.thanhan.livestreaming_system.auth.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtDecoder jwtDecoder;
    private final RedisService redisService;

    public Authentication authenticate(String token) {
        if (redisService.isInBlackList(token)) {
            throw new JwtException("Token has been revoked");
        }
        Jwt jwt = jwtDecoder.decode(token);
        return new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, List.of());
    }
}
