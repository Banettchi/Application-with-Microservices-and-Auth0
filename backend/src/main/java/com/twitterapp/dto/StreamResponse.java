package com.twitterapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * DTO wrapping the global public post stream.
 */
@Schema(description = "Global public stream of all posts")
public class StreamResponse {

    @Schema(description = "List of posts in the stream, newest first")
    private List<PostResponse> posts;

    @Schema(description = "Total number of posts in the stream", example = "42")
    private long totalPosts;

    @Schema(description = "Timestamp when the stream was retrieved")
    private Instant retrievedAt;

    public StreamResponse() {}

    public StreamResponse(List<PostResponse> posts) {
        this.posts = posts;
        this.totalPosts = posts.size();
        this.retrievedAt = Instant.now();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public List<PostResponse> getPosts() { return posts; }
    public void setPosts(List<PostResponse> posts) { this.posts = posts; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public Instant getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(Instant retrievedAt) { this.retrievedAt = retrievedAt; }
}
