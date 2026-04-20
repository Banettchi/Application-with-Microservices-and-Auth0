package com.twitterapp.controller;

import com.twitterapp.dto.CreatePostRequest;
import com.twitterapp.dto.PostResponse;
import com.twitterapp.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Post operations.
 *
 * <p>Public endpoints:
 * <ul>
 *   <li>GET /api/posts — retrieve all posts (no auth required)</li>
 * </ul>
 *
 * <p>Protected endpoints (JWT required):
 * <ul>
 *   <li>POST /api/posts — create a new post</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "Operations for creating and retrieving posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // ─── Public Endpoints ─────────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Get all posts",
        description = "Retrieves all posts in the system ordered by newest first. " +
                      "This endpoint is PUBLIC — no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))))
    })
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // ─── Protected Endpoints ──────────────────────────────────────────────────

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Create a new post",
        description = "Creates a new post for the authenticated user. " +
                      "Content must be between 1 and 140 characters. " +
                      "**Requires a valid JWT Bearer token from Auth0.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Post created successfully",
            content = @Content(schema = @Schema(implementation = PostResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request — content exceeds 140 chars or is blank",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT token",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden — token does not have required audience",
            content = @Content)
    })
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody
            @Parameter(description = "Post content (max 140 characters)", required = true)
            CreatePostRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String authorId = jwt.getSubject();
        String authorName = extractDisplayName(jwt);

        PostResponse created = postService.createPost(request, authorId, authorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Extracts the display name from JWT claims.
     * Tries "name", then "nickname", then falls back to email or subject.
     */
    private String extractDisplayName(Jwt jwt) {
        if (jwt.getClaimAsString("name") != null) {
            return jwt.getClaimAsString("name");
        }
        if (jwt.getClaimAsString("nickname") != null) {
            return jwt.getClaimAsString("nickname");
        }
        if (jwt.getClaimAsString("email") != null) {
            return jwt.getClaimAsString("email");
        }
        return jwt.getSubject();
    }
}
