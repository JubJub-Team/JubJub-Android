package com.team.jubjub.backend.post.repository;

import com.team.jubjub.backend.post.domain.PostType;
import java.util.List;

public interface PostSpatialQueryRepository {

    List<PostSearchProjection> findNearby(
            String school, PostType postType, double latitude, double longitude, int radiusMeters, int limit);

    List<PostSearchProjection> findInBounds(
            String school,
            PostType postType,
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude,
            int limit);
}
