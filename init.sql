-- Create databases for each microservice
CREATE DATABASE auth_db;
CREATE DATABASE user_db;
CREATE DATABASE feed_db;
CREATE DATABASE notification_db;
CREATE DATABASE media_db;

-- Initial user grants (optional for local, critical for prod)
-- In production, each service would have its own user.
-- For local dev, using 'admin' is acceptable simpliciy.
