package com.twitterapp.repository;

import com.twitterapp.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Post entities.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Find all posts by a given author, ordered by creation time descending.
     */
    List<Post> findByAuthorIdOrderByCreatedAtDesc(String authorId);

    /**
     * Find all posts ordered by creation time descending (newest first).
     */
    List<Post> findAllByOrderByCreatedAtDesc();
}
