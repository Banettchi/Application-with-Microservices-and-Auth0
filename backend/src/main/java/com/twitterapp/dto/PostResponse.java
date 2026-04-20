package com.twitterapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * DTO for returning post data in API responses.
 */
@Schema(description = "Post response object")
public class PostResponse {

    @Schema(description = "Unique identifier of the post", example = "1")
    private Long id;

    @Schema(description = "Content of the post (max 140 chars)", example = "Hello Twitter! #SpringBoot")
    private String content;

    @Schema(description = "Auth0 subject (user ID) of the author", example = "auth0|abc123")
    private String authorId;

    @Schema(description = "Display name of the author", example = "John Doe")
    private String authorName;

    @Schema(description = "Timestamp when the post was created")
    private Instant createdAt;

    public PostResponse() {}

    public PostResponse(Long id, String content, String authorId, String authorName, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = createdAt;
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
