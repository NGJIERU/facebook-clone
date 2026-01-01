CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    bio TEXT,
    profile_pic_url VARCHAR(255),
    cover_pic_url VARCHAR(255)
);

CREATE INDEX idx_user_profile_username ON user_profiles(username);
