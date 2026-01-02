CREATE TABLE groups_table (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(500),
    creator_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    members_count INT DEFAULT 0,
    is_public BOOLEAN DEFAULT TRUE
);

CREATE TABLE group_members (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    UNIQUE(group_id, user_id)
);

CREATE INDEX idx_groups_creator ON groups_table(creator_id);
CREATE INDEX idx_group_members_group ON group_members(group_id);
CREATE INDEX idx_group_members_user ON group_members(user_id);
