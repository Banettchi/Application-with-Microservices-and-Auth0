package com.twitterapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 *
 * <p>Registers the Bearer JWT security scheme so that protected endpoints
 * can be tested directly in the Swagger UI using a valid Auth0 access token.</p>
 *
 * <p>Swagger UI is available at: {@code /swagger-ui/index.html}</p>
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Twitter-like API",
        version = "1.0.0",
        description = "RESTful API for a simplified Twitter-like application secured with Auth0. " +
                      "Public endpoints allow reading the global post stream. " +
                      "Protected endpoints require a valid JWT Bearer token issued by Auth0.",
        contact = @Contact(
            name = "Twitter App Team",
            email = "team@twitterapp.com"
        ),
        license = @License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development Server")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "Enter the JWT Bearer token obtained from Auth0. " +
                  "Format: 'Bearer <your-token>' (the 'Bearer ' prefix is added automatically)."
)
public class OpenApiConfig {
    // Configuration is fully annotation-driven
}
