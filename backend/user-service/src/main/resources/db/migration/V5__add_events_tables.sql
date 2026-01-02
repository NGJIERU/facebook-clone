CREATE TABLE events (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(500),
    cover_image_url VARCHAR(500),
    creator_id VARCHAR(255) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    attendees_count INT DEFAULT 0,
    interested_count INT DEFAULT 0
);

CREATE TABLE event_rsvps (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(event_id, user_id)
);

CREATE INDEX idx_events_creator ON events(creator_id);
CREATE INDEX idx_events_date ON events(event_date);
CREATE INDEX idx_event_rsvps_event ON event_rsvps(event_id);
CREATE INDEX idx_event_rsvps_user ON event_rsvps(user_id);
