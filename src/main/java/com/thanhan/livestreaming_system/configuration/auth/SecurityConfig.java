package com.thanhan.livestreaming_system.configuration.auth;

import com.thanhan.livestreaming_system.auth.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = { "/auth/**",
                                                "/api/users/register",
                                                "/api/stream/on_publish",
                                                "/api/stream/finish",
                                                "/api/payment/payment_callback"
    };
    private final String[] ADMIN_ENDPOINTS = {
            "/api/users/get-all",
            "/api/users/search",
            "/api/users/total-users",
            "/api/users/{userId}",
            "/api/users/ban-user",
            "/api/users/unban-user",
            "/api/channels/get-all",
            "/api/channels/search",
            "/api/channels/total",
            "/api/channels/{channelId}",
            "/api/channels/top3-followest",
            "/api/vods/get-all",
            "/api/vods/search",
            "/api/vods/total",
            "/api/vods/stats",
            "/api/vods/top5-viewest",
            "/api/stream/get-all",
            "/api/stream/count",
            "/api/stream/stats",
            "/api/stream/ban"
    };
    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;
    private final RedisService redisService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request ->
                                request
                                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                        .requestMatchers(GET, "/api/channels/**").permitAll()
                                        .requestMatchers(GET, "/api/categories/**").permitAll()
                                        .requestMatchers(GET, "/api/vods/**").permitAll()
                                        .requestMatchers(GET, "/api/stream/**").permitAll()
                                        .requestMatchers(GET, "/api/comments/**").permitAll()
                                        .requestMatchers(GET, "/api/membership-packages/channel/**").permitAll()
                                        .requestMatchers("/api/search/**").permitAll()
                                        .requestMatchers("/api/vods/*/view").permitAll()
                                        .requestMatchers("/api/vods/*/join").permitAll()
                                        .requestMatchers(OPTIONS,"/**").permitAll()
                                        .requestMatchers("/websocket/**").permitAll()
                                        .requestMatchers("/actuator/**").permitAll()

                                        .requestMatchers("/auth/introspect").authenticated()
                                        .requestMatchers(GET, "/api/vods/upload/**").authenticated()
                                        .requestMatchers("/api/users/current-user").authenticated()
                                        .requestMatchers("/livestream/api/users/my-channel").authenticated()
                                        .requestMatchers("/api/**").authenticated()
                                        .requestMatchers(POST,"/api/users/channel/**").authenticated()
                                        .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")

                                        .anyRequest().permitAll());
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
//                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

        TokenBlackListFilter tokenBlacklistFilter = new TokenBlackListFilter(redisService);
        http.addFilterBefore(tokenBlacklistFilter, BearerTokenAuthenticationFilter.class);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        //Tat csrf chua can` thiet dung`
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");

        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "https://live-streaming-liart.vercel.app/"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Headers",
                "Access-Control-Allow-Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "Origin",
                "Cache-Control",
                "Pragma",
                "X-Requested-With"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie", "Content-Disposition", "Content-Type", "Content-Length"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        source.registerCorsConfiguration("/websocket/**", configuration);

        return source;
    }



}

