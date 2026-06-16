CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE posts (
    id UUID PRIMARY KEY,
    post_type VARCHAR(20) NOT NULL,
    school VARCHAR(100) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    location_name VARCHAR(150) NOT NULL,
    location geometry(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_posts_location_gist
    ON posts
    USING GIST (location);

CREATE INDEX idx_posts_school_type_created_at
    ON posts (school, post_type, created_at DESC);
