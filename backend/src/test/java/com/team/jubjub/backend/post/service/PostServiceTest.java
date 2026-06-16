package com.team.jubjub.backend.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team.jubjub.backend.post.api.CreatePostRequest;
import com.team.jubjub.backend.post.api.PostResponse;
import com.team.jubjub.backend.post.domain.PostEntity;
import com.team.jubjub.backend.post.domain.PostType;
import com.team.jubjub.backend.post.repository.PostJpaRepository;
import com.team.jubjub.backend.post.repository.PostSearchProjection;
import com.team.jubjub.backend.post.repository.PostSpatialQueryRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostJpaRepository postJpaRepository;

    @Mock
    private PostSpatialQueryRepository postSpatialQueryRepository;

    private PostService postService;

    @BeforeEach
    void setUp() {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        postService = new PostService(postJpaRepository, postSpatialQueryRepository, geometryFactory);
    }

    @Test
    void createPostStoresWgs84Point() {
        CreatePostRequest request =
                new CreatePostRequest(
                        PostType.LOST,
                        "Konkuk",
                        "지갑 분실",
                        "도서관 앞에서 분실",
                        "중앙도서관",
                        37.5404,
                        127.0793);

        when(postJpaRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity post = invocation.getArgument(0);
            setId(post, UUID.randomUUID());
            return post;
        });

        PostResponse response = postService.createPost(request);

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postJpaRepository).save(captor.capture());

        Point location = captor.getValue().getLocation();
        assertThat(location.getSRID()).isEqualTo(4326);
        assertThat(location.getX()).isEqualTo(request.longitude());
        assertThat(location.getY()).isEqualTo(request.latitude());
        assertThat(response.latitude()).isEqualTo(request.latitude());
        assertThat(response.longitude()).isEqualTo(request.longitude());
    }

    @Test
    void getNearbyPostsReturnsDistanceFromSpatialQuery() {
        UUID postId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-17T10:15:30+09:00");

        when(postSpatialQueryRepository.findNearby("Konkuk", PostType.FOUND, 37.54, 127.07, 500, 10))
                .thenReturn(List.of(new PostSearchProjection(
                        postId,
                        PostType.FOUND,
                        "Konkuk",
                        "에어팟 발견",
                        "정문 근처",
                        "정문",
                        37.541,
                        127.071,
                        createdAt,
                        128.4)));

        List<PostResponse> responses = postService.getNearbyPosts("Konkuk", PostType.FOUND, 37.54, 127.07, 500, 10);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(postId);
        assertThat(responses.get(0).distanceMeters()).isEqualTo(128.4);
    }

    @Test
    void getMatchedPostsReturnsOppositeTypeNearbyResults() {
        UUID sourcePostId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-17T11:00:00+09:00");
        PostEntity sourcePost = new PostEntity();
        sourcePost.setPostType(PostType.LOST);
        sourcePost.setSchool("Konkuk");

        when(postJpaRepository.findById(sourcePostId)).thenReturn(Optional.of(sourcePost));
        when(postSpatialQueryRepository.findMatches(sourcePostId, 1000, 5))
                .thenReturn(List.of(new PostSearchProjection(
                        UUID.randomUUID(),
                        PostType.FOUND,
                        "Konkuk",
                        "Found Wallet",
                        "Main gate",
                        "Gate 1",
                        37.5412,
                        127.0722,
                        createdAt,
                        220.0)));

        List<PostResponse> responses = postService.getMatchedPosts(sourcePostId, 1000, 5);

        verify(postJpaRepository).findById(sourcePostId);
        verify(postSpatialQueryRepository).findMatches(sourcePostId, 1000, 5);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).postType()).isEqualTo(PostType.FOUND);
        assertThat(responses.get(0).distanceMeters()).isEqualTo(220.0);
    }

    private void setId(PostEntity post, UUID id) {
        try {
            var field = PostEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(post, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
