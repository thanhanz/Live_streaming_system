package com.thanhan.livestreaming_system.auth.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.thanhan.livestreaming_system.auth.entity.RefreshToken;
import com.thanhan.livestreaming_system.auth.repository.RefreshTokenRepository;
import com.thanhan.livestreaming_system.auth.service.RedisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    @NonFinal
    @Value("${jwt.signer-key}")
    protected String SIGNER_KEY;

    RedisTemplate<String, String> redisTemplate;
    RefreshTokenRepository refreshTokenRepository;

    String blackListKey = "blacklist:accessToken:";

    @Override
    public void addToBlackList(String accessToken, String refreshToken) throws JOSEException, ParseException {

//        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
//
        SignedJWT signedJWT = SignedJWT.parse(accessToken);

        Long expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime().getTime();

        Long TTL = expiryTime - System.currentTimeMillis();

        String key = blackListKey + accessToken;

        log.info("Saving to Redis with key = {}, TTL = {}", key, TTL);

        redisTemplate.opsForValue().set(key, "true", TTL, TimeUnit.MILLISECONDS);

        Optional<RefreshToken> rt = refreshTokenRepository.findByToken(refreshToken);

        if (rt.isPresent()) {
            RefreshToken token = rt.get();
            log.info("Found refresh token entity: {}", token);
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        } else {
            log.warn("Refresh token not found in DB: {}", refreshToken);
        }
    }

    @Override
    public boolean isInBlackList(String token) {
        try {
            log.info("Check in blacklist");
            String key = blackListKey + token;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Check in blacklist exception", e);
            return false;
        }
    }
}
