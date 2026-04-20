package com.twitterapp.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Custom JWT validator that checks that the "aud" (audience) claim in the token
 * matches the configured Auth0 API audience.
 *
 * <p>This prevents tokens issued for a different API from being accepted
 * by this resource server.</p>
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audiences = jwt.getAudience();

        if (audiences != null && audiences.contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "The JWT token does not contain the required audience: " + audience,
                null
        );
        return OAuth2TokenValidatorResult.failure(error);
    }
}
