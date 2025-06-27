package com.thanhan.livestreaming_system.auth.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.thanhan.livestreaming_system.auth.repository.RefreshTokenRepository;
import com.thanhan.livestreaming_system.auth.service.RedisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

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

        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        Long expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime().getTime();
        String key = blackListKey + accessToken;

        redisTemplate.opsForValue().set(key, "true", expiryTime, TimeUnit.MILLISECONDS);

        refreshTokenRepository.findByToken(refreshToken).ifPresent(
                token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                }
        );

    }

    @Override
    public boolean isInBlackList(String token) {
        String key = blackListKey + token;

        return redisTemplate.hasKey(key);
    }
}
