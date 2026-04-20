package com.twitterapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration.
 *
 * <p>Configures the application as an OAuth2 Resource Server that validates
 * JWT tokens issued by Auth0. Also sets up CORS for the React frontend.</p>
 *
 * <p>Endpoint protection rules:
 * <ul>
 *   <li>PUBLIC: GET /api/posts, GET /api/stream, /swagger-ui/**, /v3/api-docs/**, /h2-console/**</li>
 *   <li>PROTECTED (JWT required): POST /api/posts, GET /api/me</li>
 * </ul>
 */
import org.springframework.context.annotation.Profile;

@Configuration
@EnableWebSecurity
@Profile("!dev")
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${auth0.audience}")
    private String audience;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (REST API with JWT — no session cookies)
            .csrf(csrf -> csrf.disable())

            // Enable CORS with our custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless sessions — each request must carry a JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Endpoint authorization rules
            .authorizeHttpRequests(auth -> auth
                // Swagger / OpenAPI — public
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                 "/v3/api-docs/**", "/v3/api-docs").permitAll()
                // H2 console — public (dev only)
                .requestMatchers("/h2-console/**").permitAll()
                // Public read endpoints
                .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stream").permitAll()
                // All other requests require a valid JWT
                .anyRequest().authenticated()
            )

            // Configure as OAuth2 Resource Server with JWT
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));

        // Allow H2 console frames (dev only)
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * Custom JWT decoder that validates both the issuer and the audience claim.
     * Auth0 tokens include an "aud" claim that must match our API audience.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);

        decoder.setJwtValidator(combinedValidator);
        return decoder;
    }

    /**
     * CORS configuration allowing the React frontend origin.
     * In production this should be restricted to the specific S3/CloudFront URL.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
