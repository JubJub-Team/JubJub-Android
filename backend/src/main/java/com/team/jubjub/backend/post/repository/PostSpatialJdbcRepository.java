package com.team.jubjub.backend.post.repository;

import com.team.jubjub.backend.post.domain.PostType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PostSpatialJdbcRepository implements PostSpatialQueryRepository {

    private static final RowMapper<PostSearchProjection> ROW_MAPPER =
            new RowMapper<>() {
                @Override
                public PostSearchProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new PostSearchProjection(
                            UUID.fromString(rs.getString("id")),
                            PostType.valueOf(rs.getString("post_type")),
                            rs.getString("school"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("location_name"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude"),
                            rs.getObject("created_at", OffsetDateTime.class),
                            (Double) rs.getObject("distance_meters"));
                }
            };

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PostSpatialJdbcRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<PostSearchProjection> findMatches(UUID sourcePostId, int radiusMeters, int limit) {
        String sql =
                """
                SELECT
                    candidate.id,
                    candidate.post_type,
                    candidate.school,
                    candidate.title,
                    candidate.content,
                    candidate.location_name,
                    ST_Y(candidate.location) AS latitude,
                    ST_X(candidate.location) AS longitude,
                    candidate.created_at,
                    ST_Distance(
                        candidate.location::geography,
                        source.location::geography
                    ) AS distance_meters
                FROM posts source
                JOIN posts candidate
                  ON candidate.id <> source.id
                 AND candidate.school = source.school
                 AND candidate.post_type <> source.post_type
                WHERE source.id = :sourcePostId
                  -- Pair lost/found posts by the same campus radius in meter units.
                  AND ST_DWithin(
                      candidate.location::geography,
                      source.location::geography,
                      :radiusMeters
                  )
                ORDER BY distance_meters ASC, candidate.created_at DESC
                LIMIT :limit
                """;

        return namedParameterJdbcTemplate.query(
                sql, Map.of("sourcePostId", sourcePostId, "radiusMeters", radiusMeters, "limit", limit), ROW_MAPPER);
    }

    @Override
    public List<PostSearchProjection> findNearby(
            String school, PostType postType, double latitude, double longitude, int radiusMeters, int limit) {
        String sql =
                """
                SELECT
                    id,
                    post_type,
                    school,
                    title,
                    content,
                    location_name,
                    ST_Y(location) AS latitude,
                    ST_X(location) AS longitude,
                    created_at,
                    ST_Distance(
                        location::geography,
                        ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
                    ) AS distance_meters
                FROM posts
                WHERE (:school IS NULL OR school = :school)
                  AND (:postType IS NULL OR post_type = :postType)
                  -- geography cast keeps the search radius in meters instead of degrees.
                  AND ST_DWithin(
                      location::geography,
                      ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                      :radiusMeters
                  )
                ORDER BY distance_meters ASC, created_at DESC
                LIMIT :limit
                """;

        return namedParameterJdbcTemplate.query(
                sql,
                createParams(
                        school,
                        postType,
                        Map.of("latitude", latitude, "longitude", longitude, "radiusMeters", radiusMeters, "limit", limit)),
                ROW_MAPPER);
    }

    @Override
    public List<PostSearchProjection> findInBounds(
            String school,
            PostType postType,
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude,
            int limit) {
        String sql =
                """
                SELECT
                    id,
                    post_type,
                    school,
                    title,
                    content,
                    location_name,
                    ST_Y(location) AS latitude,
                    ST_X(location) AS longitude,
                    created_at,
                    NULL::double precision AS distance_meters
                FROM posts
                WHERE (:school IS NULL OR school = :school)
                  AND (:postType IS NULL OR post_type = :postType)
                  AND location && ST_MakeEnvelope(:minLongitude, :minLatitude, :maxLongitude, :maxLatitude, 4326)
                  AND ST_Intersects(
                      location,
                      ST_MakeEnvelope(:minLongitude, :minLatitude, :maxLongitude, :maxLatitude, 4326)
                  )
                ORDER BY created_at DESC
                LIMIT :limit
                """;

        return namedParameterJdbcTemplate.query(
                sql,
                createParams(
                        school,
                        postType,
                        Map.of(
                                "minLatitude", minLatitude,
                                "minLongitude", minLongitude,
                                "maxLatitude", maxLatitude,
                                "maxLongitude", maxLongitude,
                                "limit", limit)),
                ROW_MAPPER);
    }

    private Map<String, Object> createParams(String school, PostType postType, Map<String, Object> extraParams) {
        Map<String, Object> params = new HashMap<>(extraParams);
        params.put("school", school);
        params.put("postType", postType != null ? postType.name() : null);
        return params;
    }
}
