package com.twitterapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Development-only Security configuration.
 *
 * <p>Active when Spring profile "dev" is set. Uses a local symmetric HMAC key
 * to sign/verify JWTs so Auth0 is NOT required. All endpoints that need auth
 * can be tested by generating a local dev token.</p>
 *
 * <p>This config is NEVER active in production (profile "dev" only).</p>
 *
 * <p>To generate a dev token for testing, send a POST to /api/dev/token
 * (see DevController).</p>
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfig {

    // A fixed 32-byte secret used only in dev mode (not a real secret)
    private static final String DEV_SECRET =
            "dev-only-secret-key-32-bytes-ok!";

    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(devCorsSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                 "/v3/api-docs/**", "/v3/api-docs").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/dev/**").permitAll()    // dev token helper
                .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stream").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.decoder(devJwtDecoder())));

        http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }

    /**
     * HMAC-SHA256 JWT decoder using the dev-only symmetric secret.
     * No network calls, no Auth0 required.
     */
    @Bean
    public JwtDecoder devJwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(
            DEV_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public CorsConfigurationSource devCorsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
