package com.team.jubjub.backend.post.service;

import com.team.jubjub.backend.post.api.CreatePostRequest;
import com.team.jubjub.backend.post.api.PostResponse;
import com.team.jubjub.backend.post.domain.PostEntity;
import com.team.jubjub.backend.post.domain.PostType;
import com.team.jubjub.backend.post.repository.PostJpaRepository;
import com.team.jubjub.backend.post.repository.PostSearchProjection;
import com.team.jubjub.backend.post.repository.PostSpatialQueryRepository;
import java.util.List;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostJpaRepository postJpaRepository;
    private final PostSpatialQueryRepository postSpatialQueryRepository;
    private final GeometryFactory geometryFactory;

    public PostService(
            PostJpaRepository postJpaRepository,
            PostSpatialQueryRepository postSpatialQueryRepository,
            GeometryFactory geometryFactory) {
        this.postJpaRepository = postJpaRepository;
        this.postSpatialQueryRepository = postSpatialQueryRepository;
        this.geometryFactory = geometryFactory;
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        PostEntity post = new PostEntity();
        post.setPostType(request.postType());
        post.setSchool(request.school());
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setLocationName(request.locationName());
        post.setLocation(createPoint(request.latitude(), request.longitude()));

        PostEntity savedPost = postJpaRepository.save(post);
        return toResponse(savedPost);
    }

    public PostResponse getPost(UUID postId) {
        return toResponse(getRequiredPost(postId));
    }

    public List<PostResponse> getMatchedPosts(UUID postId, int radiusMeters, int limit) {
        getRequiredPost(postId);
        return postSpatialQueryRepository.findMatches(postId, radiusMeters, limit).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PostResponse> getNearbyPosts(
            String school, PostType postType, double latitude, double longitude, int radiusMeters, int limit) {
        return postSpatialQueryRepository.findNearby(school, postType, latitude, longitude, radiusMeters, limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PostResponse> getPostsInBounds(
            String school,
            PostType postType,
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude,
            int limit) {
        return postSpatialQueryRepository
                .findInBounds(school, postType, minLatitude, minLongitude, maxLatitude, maxLongitude, limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Point createPoint(double latitude, double longitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    private PostResponse toResponse(PostEntity post) {
        return new PostResponse(
                post.getId(),
                post.getPostType(),
                post.getSchool(),
                post.getTitle(),
                post.getContent(),
                post.getLocationName(),
                post.getLocation().getY(),
                post.getLocation().getX(),
                post.getCreatedAt(),
                null);
    }

    private PostResponse toResponse(PostSearchProjection projection) {
        return new PostResponse(
                projection.id(),
                projection.postType(),
                projection.school(),
                projection.title(),
                projection.content(),
                projection.locationName(),
                projection.latitude(),
                projection.longitude(),
                projection.createdAt(),
                projection.distanceMeters());
    }

    private PostEntity getRequiredPost(UUID postId) {
        return postJpaRepository
                .findById(postId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Post not found"));
    }
}
