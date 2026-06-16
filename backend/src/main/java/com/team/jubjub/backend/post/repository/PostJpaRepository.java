package com.team.jubjub.backend.post.repository;

import com.team.jubjub.backend.post.domain.PostEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<PostEntity, UUID> {}
