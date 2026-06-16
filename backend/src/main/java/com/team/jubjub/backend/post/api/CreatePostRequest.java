package com.team.jubjub.backend.post.api;

import com.team.jubjub.backend.post.domain.PostType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotNull PostType postType,
        @NotBlank @Size(max = 100) String school,
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 2000) String content,
        @NotBlank @Size(max = 150) String locationName,
        @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
        @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude) {}
