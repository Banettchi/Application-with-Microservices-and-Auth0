package com.twitterapp.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * DEV-ONLY controller for generating local JWT tokens without Auth0.
 *
 * <p>This controller is ONLY active when the "dev" Spring profile is set.
 * It is NOT compiled or loaded in production.</p>
 *
 * <p>Usage: POST /api/dev/token with JSON body {"name":"Your Name","email":"you@test.com"}
 * Returns a signed JWT you can paste into the Swagger UI Bearer token field.</p>
 */
@RestController
@RequestMapping("/api/dev")
@Profile("dev")
@Tag(name = "Dev Tools", description = "Development-only helpers (not available in production)")
public class DevController {

    private static final String DEV_SECRET = "dev-only-secret-key-32-bytes-ok!";

    @PostMapping("/token")
    @Operation(
        summary = "[DEV ONLY] Generate a local JWT token",
        description = "Creates a signed JWT using the dev HMAC secret. " +
                      "Use the returned token as the Bearer token in Swagger UI or the frontend app.js for local testing."
    )
    public ResponseEntity<Map<String, String>> generateDevToken(
            @RequestBody(required = false) Map<String, String> body) throws Exception {

        String name  = body != null ? body.getOrDefault("name", "Dev User") : "Dev User";
        String email = body != null ? body.getOrDefault("email", "dev@local.test") : "dev@local.test";
        String sub   = "dev|local-" + System.currentTimeMillis();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(sub)
                .issuer("dev-local")
                .claim("name", name)
                .claim("email", email)
                .claim("nickname", name.split(" ")[0].toLowerCase())
                .claim("picture", "https://ui-avatars.com/api/?name=" +
                        java.net.URLEncoder.encode(name, StandardCharsets.UTF_8) +
                        "&background=6366f1&color=fff&rounded=true")
                .expirationTime(Date.from(Instant.now().plusSeconds(3600 * 8))) // 8 hours
                .issueTime(new Date())
                .build();

        MACSigner signer = new MACSigner(DEV_SECRET.getBytes(StandardCharsets.UTF_8));
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);

        String token = jwt.serialize();

        return ResponseEntity.ok(Map.of(
                "token", token,
                "usage", "Use as: Authorization: Bearer " + token.substring(0, 20) + "...",
                "expiresIn", "8 hours",
                "name", name,
                "email", email
        ));
    }
}
