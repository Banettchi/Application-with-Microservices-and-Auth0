package com.twitterapp.controller;

import com.twitterapp.dto.StreamResponse;
import com.twitterapp.service.StreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the global public Stream (feed).
 *
 * <p>All endpoints in this controller are PUBLIC — no authentication required.</p>
 */
@RestController
@RequestMapping("/api/stream")
@Tag(name = "Stream", description = "Global public feed of all posts")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    @GetMapping
    @Operation(
        summary = "Get the global public stream",
        description = "Returns the single global public stream containing all posts ordered by newest first. " +
                      "This endpoint is PUBLIC — no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stream retrieved successfully",
            content = @Content(schema = @Schema(implementation = StreamResponse.class)))
    })
    public ResponseEntity<StreamResponse> getStream() {
        return ResponseEntity.ok(streamService.getGlobalStream());
    }
}
