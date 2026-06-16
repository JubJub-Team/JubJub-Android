CREATE INDEX idx_posts_location_geography_gist
    ON posts
    USING GIST ((location::geography));
