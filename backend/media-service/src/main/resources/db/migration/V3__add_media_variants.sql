-- Add image variant URL columns to media table
ALTER TABLE media
ADD COLUMN thumbnail_url VARCHAR(500),
ADD COLUMN small_url VARCHAR(500),
ADD COLUMN medium_url VARCHAR(500);
