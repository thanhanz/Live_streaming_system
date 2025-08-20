package com.thanhan.livestreaming_system.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = { "/auth/log-in",
                                                "/auth/introspect",
                                                "/api/users/register",
                                                "/auth/refresh",
                                                "/auth/logout",
                                                "/api/stream/**",
                                                "/watch", //Test in thymeleaf
                                                "/api/vods/**"
    };

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    @Bean
    public SecurityFilterChain publicEndpoints(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request ->
                                request .requestMatchers(OPTIONS,"/**").permitAll()
                                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                        .requestMatchers("/websocket/**").permitAll()
                                        .requestMatchers("/actuator/**").permitAll()

                                        .anyRequest().authenticated());
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder()))
                                //Covert "SCOPE_... to ROLE_..."
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
//                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

        //Cai dat CORS de co the ket noi den Browser
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        //Tat csrf chua can` thiet dung`
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
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
        configuration.addAllowedOrigin("http://localhost:3000");
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
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie", "Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        source.registerCorsConfiguration("/websocket/**", configuration);

        return source;
    }



}

