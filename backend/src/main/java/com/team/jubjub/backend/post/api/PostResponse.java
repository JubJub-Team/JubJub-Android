package com.team.jubjub.backend.post.api;

import com.team.jubjub.backend.post.domain.PostType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PostResponse(
        UUID id,
        PostType postType,
        String school,
        String title,
        String content,
        String locationName,
        Double latitude,
        Double longitude,
        OffsetDateTime createdAt,
        Double distanceMeters) {}
