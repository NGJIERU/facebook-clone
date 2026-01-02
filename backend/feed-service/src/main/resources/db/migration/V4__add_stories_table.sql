CREATE TABLE stories (
    id UUID PRIMARY KEY,
    author_id VARCHAR(255) NOT NULL,
    image_url VARCHAR(500),
    text TEXT,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    views_count INT DEFAULT 0
);

CREATE INDEX idx_stories_author ON stories(author_id);
CREATE INDEX idx_stories_expires ON stories(expires_at);
