package com.twitterapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for creating a new post.
 */
@Schema(description = "Request body for creating a new post")
public class CreatePostRequest {

    @NotBlank(message = "Post content cannot be blank")
    @Size(min = 1, max = 140, message = "Post content must be between 1 and 140 characters")
    @Schema(description = "The content of the post", example = "Hello Twitter! #SpringBoot", maxLength = 140, requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    public CreatePostRequest() {}

    public CreatePostRequest(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
