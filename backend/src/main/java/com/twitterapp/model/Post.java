package com.twitterapp.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a post in the Twitter-like application.
 * Content is limited to 140 characters.
 */
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String content;

    /** Auth0 subject (sub claim) of the author */
    @Column(nullable = false)
    private String authorId;

    /** Display name of the author extracted from the JWT token */
    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public Post() {}

    public Post(String content, String authorId, String authorName) {
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
