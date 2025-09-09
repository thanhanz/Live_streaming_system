package com.thanhan.livestreaming_system.auth.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.thanhan.livestreaming_system.auth.dto.request.AuthenticationRequest;
import com.thanhan.livestreaming_system.auth.dto.request.IntrospectRequest;
import com.thanhan.livestreaming_system.auth.dto.request.LogoutRequest;
import com.thanhan.livestreaming_system.auth.dto.response.AuthenticationResponse;
import com.thanhan.livestreaming_system.auth.dto.response.IntrospectResponse;
import com.thanhan.livestreaming_system.auth.entity.RefreshToken;
import com.thanhan.livestreaming_system.auth.repository.RefreshTokenRepository;
import com.thanhan.livestreaming_system.auth.service.AuthenticationService;
import com.thanhan.livestreaming_system.auth.service.RedisService;
import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;
    RedisService redisService;

    @NonFinal
    @Value("${jwt.signer-key}")
    protected String SIGNER_KEY;

    protected final long expirationTime = 1; //15' cho access Token


    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws JOSEException {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);
        var refreshToken = UUID.randomUUID().toString();

        createNewRefreshToken(refreshToken, user);

        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .isAuthenticated(true)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var accessToken = request.getToken();

        boolean isLogout = redisService.isInBlackList(accessToken);

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(accessToken);

        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        log.info("Verified: {}", verified);
        log.info("Expired: {}", expiration.after(new Date()));
        log.info("Is logout: {}", isLogout);

        return IntrospectResponse.builder()
                .isValid(verified && expiration.after(new Date()) && !isLogout)
                .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(String refreshToken) throws JOSEException {
        RefreshToken oldToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found"));

        if (oldToken.isRevoked() || oldToken.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.REVOKED_TOKEN);
        }
        User user = oldToken.getUser();

        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        var newAccessToken = generateToken(user);
        var newRefreshToken = UUID.randomUUID().toString();

        createNewRefreshToken(newRefreshToken, user);

        return AuthenticationResponse.builder()
                .refreshToken(newRefreshToken)
                .accessToken(newAccessToken)
                .isAuthenticated(true)
                .build();
    }

    @Override
    public void logout(LogoutRequest request, String refreshToken) throws JOSEException, ParseException {
        redisService.addToBlackList(request.getAccessToken(), refreshToken);
    }

    private void createNewRefreshToken(String token, User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);
    }

    private String generateToken(User user) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("auth-service")
                .issueTime(new Date())
                /*
                 *  #Remember change to MINUTE (Hours for testing)
                 */
                .expirationTime(new Date(
                        Instant.now().plus(expirationTime, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());
        //Need header + payload
        JWSObject object = new JWSObject(header, payload);

        try {
            object.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return object.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token: ",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner scopeJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(roles -> {
                scopeJoiner.add("ROLE_" + roles.getName());
            });

        return scopeJoiner.toString();
    }

}
