package com.twitterapp.service;

import com.twitterapp.dto.StreamResponse;
import com.twitterapp.dto.PostResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for the global public Stream (feed).
 */
@Service
public class StreamService {

    private final PostService postService;

    public StreamService(PostService postService) {
        this.postService = postService;
    }

    /**
     * Returns the global public stream of all posts, newest first.
     *
     * @return StreamResponse containing all posts
     */
    public StreamResponse getGlobalStream() {
        List<PostResponse> posts = postService.getAllPosts();
        return new StreamResponse(posts);
    }
}
