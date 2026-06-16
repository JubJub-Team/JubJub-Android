package com.team.jubjub.backend.post.api;

import com.team.jubjub.backend.post.domain.PostType;
import com.team.jubjub.backend.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable UUID postId) {
        return postService.getPost(postId);
    }

    @GetMapping("/{postId}/matches")
    public List<PostResponse> getMatchedPosts(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "1000") @Min(100) @Max(10000) int radiusMeters,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return postService.getMatchedPosts(postId, radiusMeters, limit);
    }

    @GetMapping("/nearby")
    public List<PostResponse> getNearbyPosts(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) PostType postType,
            @RequestParam @NotNull Double latitude,
            @RequestParam @NotNull Double longitude,
            @RequestParam(defaultValue = "3000") @Min(100) @Max(50000) int radiusMeters,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return postService.getNearbyPosts(school, postType, latitude, longitude, radiusMeters, limit);
    }

    @GetMapping("/map")
    public List<PostResponse> getPostsInBounds(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) PostType postType,
            @RequestParam @NotNull Double minLatitude,
            @RequestParam @NotNull Double minLongitude,
            @RequestParam @NotNull Double maxLatitude,
            @RequestParam @NotNull Double maxLongitude,
            @RequestParam(defaultValue = "100") @Min(1) @Max(300) int limit) {
        return postService.getPostsInBounds(
                school, postType, minLatitude, minLongitude, maxLatitude, maxLongitude, limit);
    }
}
