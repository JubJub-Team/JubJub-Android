package com.team.jubjub.backend.post.repository;

import com.team.jubjub.backend.post.domain.PostType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PostSearchProjection(
        UUID id,
        PostType postType,
        String school,
        String title,
        String content,
        String locationName,
        double latitude,
        double longitude,
        OffsetDateTime createdAt,
        Double distanceMeters) {}
