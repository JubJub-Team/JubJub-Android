package com.team.jubjub.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team.jubjub.backend.post.domain.PostEntity;
import com.team.jubjub.backend.post.domain.PostType;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostSpatialJdbcRepositoryIntegrationTest {

    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>(POSTGIS_IMAGE).withDatabaseName("jubjub").withUsername("jubjub").withPassword("jubjub");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private PostSpatialQueryRepository postSpatialQueryRepository;

    @Autowired
    private GeometryFactory geometryFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        postJpaRepository.deleteAll();
    }

    @Test
    void findNearbyReturnsPostsWithinRadiusOrderedByDistance() {
        savePost("Konkuk", PostType.FOUND, 37.5406, 127.0765, "near-found");
        savePost("Konkuk", PostType.FOUND, 37.5430, 127.0810, "far-found");
        savePost("Konkuk", PostType.LOST, 37.5407, 127.0766, "lost-post");

        List<PostSearchProjection> results =
                postSpatialQueryRepository.findNearby("Konkuk", PostType.FOUND, 37.5405, 127.0764, 500, 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("near-found");
        assertThat(results.get(0).distanceMeters()).isNotNull();
    }

    @Test
    void findMatchesReturnsOppositeTypePostsInSameSchoolWithinRadius() {
        PostEntity source = savePost("Konkuk", PostType.LOST, 37.5410, 127.0770, "source-lost");
        savePost("Konkuk", PostType.FOUND, 37.5412, 127.0771, "matched-found");
        savePost("Konkuk", PostType.LOST, 37.5411, 127.0772, "same-type");
        savePost("Hongik", PostType.FOUND, 37.5412, 127.0771, "other-school");
        savePost("Konkuk", PostType.FOUND, 37.5500, 127.0900, "outside-radius");

        List<PostSearchProjection> results = postSpatialQueryRepository.findMatches(source.getId(), 500, 10);

        assertThat(results).extracting(PostSearchProjection::title).containsExactly("matched-found");
        assertThat(results.get(0).distanceMeters()).isNotNull();
    }

    @Test
    void explainAnalyzeForNearbyQueryUsesGeographyGistIndex() {
        seedExplainRows();
        jdbcTemplate.execute("ANALYZE posts");

        List<String> planLines =
                jdbcTemplate.query(
                        """
                        EXPLAIN ANALYZE
                        SELECT id
                        FROM posts
                        WHERE school = 'Konkuk'
                          AND post_type = 'FOUND'
                          AND ST_DWithin(
                              location::geography,
                              ST_SetSRID(ST_MakePoint(127.0764, 37.5405), 4326)::geography,
                              500
                          )
                        ORDER BY ST_Distance(
                            location::geography,
                            ST_SetSRID(ST_MakePoint(127.0764, 37.5405), 4326)::geography
                        ) ASC
                        LIMIT 10
                        """,
                        (rs, rowNum) -> rs.getString(1));

        String plan = String.join("\n", planLines);

        assertThat(plan).contains("idx_posts_location_geography_gist");
        assertThat(plan).containsAnyOf("Index Scan", "Bitmap Index Scan", "Bitmap Heap Scan");
    }

    private PostEntity savePost(String school, PostType postType, double latitude, double longitude, String title) {
        PostEntity post = new PostEntity();
        post.setSchool(school);
        post.setPostType(postType);
        post.setTitle(title);
        post.setContent(title + "-content");
        post.setLocationName(title + "-place");
        post.setLocation(createPoint(latitude, longitude));
        return postJpaRepository.save(post);
    }

    private Point createPoint(double latitude, double longitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    private void seedExplainRows() {
        List<Object[]> batchArguments = new ArrayList<>();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-17T12:00:00+09:00");

        for (int index = 0; index < 2500; index++) {
            double latitude = 35.0 + (index % 100) * 0.0005;
            double longitude = 128.0 + (index % 100) * 0.0005;
            batchArguments.add(createInsertArguments("Konkuk", "FOUND", "bulk-" + index, latitude, longitude, createdAt));
        }

        batchArguments.add(createInsertArguments("Konkuk", "FOUND", "match-1", 37.54055, 127.07645, createdAt));
        batchArguments.add(createInsertArguments("Konkuk", "FOUND", "match-2", 37.54065, 127.07655, createdAt));

        jdbcTemplate.batchUpdate(
                """
                INSERT INTO posts (
                    id,
                    post_type,
                    school,
                    title,
                    content,
                    location_name,
                    location,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?)
                """,
                batchArguments);
    }

    private Object[] createInsertArguments(
            String school, String postType, String title, double latitude, double longitude, OffsetDateTime createdAt) {
        return new Object[] {
            UUID.randomUUID(),
            postType,
            school,
            title,
            title + "-content",
            title + "-place",
            longitude,
            latitude,
            Timestamp.from(createdAt.toInstant())
        };
    }
}
