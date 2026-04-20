package com.twitterapp.model;

import java.time.Instant;
import java.util.List;

/**
 * Represents the single global public stream (feed) of all posts.
 * This is a virtual entity — it is assembled from the posts stored in the DB.
 */
public class Stream {

    private List<Post> posts;
    private long totalPosts;
    private Instant retrievedAt;

    public Stream(List<Post> posts) {
        this.posts = posts;
        this.totalPosts = posts.size();
        this.retrievedAt = Instant.now();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public Instant getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(Instant retrievedAt) { this.retrievedAt = retrievedAt; }
}
