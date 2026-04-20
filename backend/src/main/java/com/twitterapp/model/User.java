package com.twitterapp.model;

/**
 * Represents a system user derived from the Auth0 JWT token.
 * Users are NOT stored in the database; their identity comes entirely
 * from the JWT claims issued by Auth0.
 */
public class User {

    private String id;          // Auth0 sub claim
    private String email;       // email claim
    private String name;        // name claim
    private String picture;     // picture claim (avatar URL)

    public User() {}

    public User(String id, String email, String name, String picture) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
}
