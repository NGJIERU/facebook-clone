CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255)
);

CREATE INDEX idx_user_email ON users(email);
