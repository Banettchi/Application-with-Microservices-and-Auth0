package com.twitterapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing the current authenticated user's profile.
 * Data is extracted from Auth0 JWT claims.
 */
@Schema(description = "Current authenticated user information")
public class UserInfoResponse {

    @Schema(description = "Auth0 subject identifier", example = "auth0|64abc123def456")
    private String sub;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's display name", example = "John Doe")
    private String name;

    @Schema(description = "User's avatar picture URL", example = "https://avatars.auth0.com/user.png")
    private String picture;

    public UserInfoResponse() {}

    public UserInfoResponse(String sub, String email, String name, String picture) {
        this.sub = sub;
        this.email = email;
        this.name = name;
        this.picture = picture;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
}
