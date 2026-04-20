package com.twitterapp.controller;

import com.twitterapp.dto.PostResponse;
import com.twitterapp.dto.UserInfoResponse;
import com.twitterapp.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for User operations.
 *
 * <p>All endpoints in this controller are PROTECTED — a valid JWT is required.</p>
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Users", description = "User profile operations")
public class UserController {

    private final PostService postService;

    public UserController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Returns the current authenticated user's profile extracted from JWT claims.
     * Satisfies the mandatory GET /api/me endpoint requirement.
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get current user info",
        description = "Returns the profile of the currently authenticated user, " +
                      "extracted from the Auth0 JWT token claims. " +
                      "**Requires a valid JWT Bearer token from Auth0.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User info retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT token",
            content = @Content)
    })
    public ResponseEntity<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UserInfoResponse userInfo = new UserInfoResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("picture")
        );
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Returns all posts created by the currently authenticated user.
     */
    @GetMapping("/me/posts")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get my posts",
        description = "Returns all posts created by the currently authenticated user, newest first. " +
                      "**Requires a valid JWT Bearer token from Auth0.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT token",
            content = @Content)
    })
    public ResponseEntity<List<PostResponse>> getMyPosts(@AuthenticationPrincipal Jwt jwt) {
        List<PostResponse> myPosts = postService.getPostsByAuthor(jwt.getSubject());
        return ResponseEntity.ok(myPosts);
    }
}
