-- Add share-related fields to feed_posts
ALTER TABLE feed_posts ADD COLUMN shares_count INT DEFAULT 0;
ALTER TABLE feed_posts ADD COLUMN original_post_id UUID;
ALTER TABLE feed_posts ADD COLUMN original_author_id VARCHAR(255);

CREATE INDEX idx_feed_original_post ON feed_posts(original_post_id);
