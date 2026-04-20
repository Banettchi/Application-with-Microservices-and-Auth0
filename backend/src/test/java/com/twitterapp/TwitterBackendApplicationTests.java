package com.twitterapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitterapp.dto.CreatePostRequest;
import com.twitterapp.model.Post;
import com.twitterapp.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Twitter-like REST API.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Public access to GET /api/posts and GET /api/stream</li>
 *   <li>Protected access to POST /api/posts and GET /api/me</li>
 *   <li>401 Unauthorized for missing JWT</li>
 *   <li>400 Bad Request for content validation (>140 chars)</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    // Disable OAuth2 resource server config during tests (use mock JWT)
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test-domain.auth0.com/",
    "auth0.audience=test-audience"
})
class TwitterBackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        // Seed one post for read tests
        Post seedPost = new Post("Hello from seed data!", "auth0|seed123", "Seed User");
        postRepository.save(seedPost);
    }

    // ─── Public Endpoints ─────────────────────────────────────────────────────

    @Test
    void givenNoAuth_whenGetPosts_thenReturn200() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[0].content", is("Hello from seed data!")));
    }

    @Test
    void givenNoAuth_whenGetStream_thenReturn200() throws Exception {
        mockMvc.perform(get("/api/stream"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", isA(java.util.List.class)))
                .andExpect(jsonPath("$.totalPosts", greaterThanOrEqualTo(1)));
    }

    // ─── Protected Endpoints — Unauthorized ───────────────────────────────────

    @Test
    void givenNoAuth_whenPostPost_thenReturn401() throws Exception {
        CreatePostRequest request = new CreatePostRequest("Unauthorized post");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenNoAuth_whenGetMe_thenReturn401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Protected Endpoints — With JWT ───────────────────────────────────────

    @Test
    void givenValidJwt_whenPostPost_thenReturn201() throws Exception {
        CreatePostRequest request = new CreatePostRequest("Test post from authenticated user!");

        mockMvc.perform(post("/api/posts")
                        .with(jwt()
                            .jwt(builder -> builder
                                .subject("auth0|testuser123")
                                .claim("name", "Test User")
                                .claim("email", "test@example.com")
                                .claim("aud", java.util.List.of("test-audience"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is("Test post from authenticated user!")))
                .andExpect(jsonPath("$.authorName", is("Test User")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void givenValidJwt_whenGetMe_thenReturn200() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(jwt()
                            .jwt(builder -> builder
                                .subject("auth0|testuser123")
                                .claim("name", "Test User")
                                .claim("email", "test@example.com")
                                .claim("picture", "https://avatar.example.com/pic.jpg")
                                .claim("aud", java.util.List.of("test-audience")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub", is("auth0|testuser123")))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    // ─── Validation Tests ─────────────────────────────────────────────────────

    @Test
    void givenValidJwt_whenPostWithContentOver140Chars_thenReturn400() throws Exception {
        String longContent = "A".repeat(141);
        CreatePostRequest request = new CreatePostRequest(longContent);

        mockMvc.perform(post("/api/posts")
                        .with(jwt()
                            .jwt(builder -> builder
                                .subject("auth0|testuser123")
                                .claim("name", "Test User")
                                .claim("aud", java.util.List.of("test-audience"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.content", containsString("140")));
    }

    @Test
    void givenValidJwt_whenPostWithBlankContent_thenReturn400() throws Exception {
        CreatePostRequest request = new CreatePostRequest("");

        mockMvc.perform(post("/api/posts")
                        .with(jwt()
                            .jwt(builder -> builder
                                .subject("auth0|testuser123")
                                .claim("name", "Test User")
                                .claim("aud", java.util.List.of("test-audience"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
