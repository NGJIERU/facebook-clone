CREATE TABLE feed_posts (
    id UUID PRIMARY KEY,
    author_id VARCHAR(255) NOT NULL,
    content TEXT,
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0
);

CREATE INDEX idx_feed_author_id ON feed_posts(author_id);
CREATE INDEX idx_feed_created_at ON feed_posts(created_at DESC);
