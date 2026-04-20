package com.twitterapp.service;

import com.twitterapp.dto.CreatePostRequest;
import com.twitterapp.dto.PostResponse;
import com.twitterapp.model.Post;
import com.twitterapp.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Post operations.
 */
@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Creates a new post for the authenticated user.
     *
     * @param request   the post content DTO
     * @param authorId  Auth0 sub claim of the author
     * @param authorName display name of the author
     * @return the saved post as a response DTO
     */
    public PostResponse createPost(CreatePostRequest request, String authorId, String authorName) {
        Post post = new Post(request.getContent(), authorId, authorName);
        Post saved = postRepository.save(post);
        return toResponse(saved);
    }

    /**
     * Retrieves all posts ordered by newest first.
     *
     * @return list of post response DTOs
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all posts by a specific author.
     *
     * @param authorId Auth0 sub claim of the author
     * @return list of post response DTOs
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByAuthor(String authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getAuthorId(),
                post.getAuthorName(),
                post.getCreatedAt()
        );
    }
}
