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
import com.thanhan.livestreaming_system.auth.dto.response.GoogleTokenResponse;
import com.thanhan.livestreaming_system.auth.dto.response.IntrospectResponse;
import com.thanhan.livestreaming_system.auth.entity.RefreshToken;
import com.thanhan.livestreaming_system.auth.repository.RefreshTokenRepository;
import com.thanhan.livestreaming_system.auth.service.AuthenticationService;
import com.thanhan.livestreaming_system.auth.service.RedisService;
import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.user.entity.Provider;
import com.thanhan.livestreaming_system.user.entity.Role;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.repository.UserRepository;
import com.thanhan.livestreaming_system.user.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;
    RedisService redisService;
    RestTemplate restTemplate = new RestTemplate();
    RoleService roleService;
    @NonFinal
    @Value("${jwt.signer-key}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleReturnUri;

    protected final long expirationTime = 1; //15' cho access Token


    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws JOSEException {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (!user.getActive())
            throw new AppException(ErrorCode.USER_ACCOUNT_BANNED);

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
            log.error("Cannot create token: ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner scopeJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(roles -> {
                scopeJoiner.add("ROLE_" + roles.getName());
            });

        return scopeJoiner.toString();
    }

    @Override
    public String generateUrlLoginType(String loginType, HttpSession session) {
        switch (loginType) {
            case "google":
                String state = UUID.randomUUID().toString();
                String nonce = UUID.randomUUID().toString();

                session.setAttribute("oauth2_state", state);
                session.setAttribute("oauth2_nonce", nonce);

                return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                        .queryParam("client_id", googleClientId)
                        .queryParam("redirect_uri", googleReturnUri)
                        .queryParam("response_type", "code")
                        .queryParam("scope", "openid profile email")
                        .queryParam("state", state)
                        .queryParam("nonce", nonce)
                        .build().toUriString();
        }

        throw new IllegalArgumentException("Unsupported login type: " + loginType);
    }

    @Override
    @Transactional
    public AuthenticationResponse googleLoginCallback(String code, String state, HttpSession session) throws ParseException, JOSEException {

        String sessionState = (String) session.getAttribute("oauth2_state");
        if (sessionState == null || !sessionState.equals(state)) {
            throw new RuntimeException("Invalid OAuth2 state");
        }

        String tokenUrl = "https://oauth2.googleapis.com/token";

        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", googleClientId);
        params.put("client_secret", googleClientSecret);
        params.put("redirect_uri", googleReturnUri);
        params.put("grant_type", "authorization_code");

        GoogleTokenResponse tokenResponse = restTemplate.postForObject(
                tokenUrl, params, GoogleTokenResponse.class);

        if (tokenResponse == null || tokenResponse.idToken() == null) {
            throw new RuntimeException("Cannot get id_token from Google");
        }

        SignedJWT signedJWT = SignedJWT.parse(tokenResponse.idToken());
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        String sessionNonce = (String) session.getAttribute("oauth2_nonce");
        if (!claims.getStringClaim("nonce").equals(sessionNonce)) {
            throw new RuntimeException("Invalid nonce");
        }

        String providerId = claims.getSubject();
        String email = claims.getStringClaim("email");
        String name = claims.getStringClaim("name");
        String picture = claims.getStringClaim("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name);
                    newUser.setAvatar(picture);
                    newUser.setProvider(Provider.GOOGLE);
                    newUser.setProviderId(providerId);
                    newUser.setActive(true);
                    Role userRole = roleService.getRole("USER");
                    Set<Role> roles = new HashSet<>();
                    roles.add(userRole);
                    newUser.setRoles(roles);
                    return userRepository.save(newUser);
                });

        if (!user.getActive())
            throw new AppException(ErrorCode.USER_ACCOUNT_BANNED);

        String accessToken = generateToken(user);
        String refreshToken = UUID.randomUUID().toString();
        createNewRefreshToken(refreshToken, user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isAuthenticated(true)
                .build();
    }
}
