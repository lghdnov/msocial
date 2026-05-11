CREATE TABLE IF NOT EXISTS auth_sessions (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    refresh_token           VARCHAR(512) NOT NULL UNIQUE,
    refresh_token_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at              TIMESTAMP WITH TIME ZONE,
    version                 BIGINT DEFAULT 0
);

CREATE INDEX idx_auth_sessions_refresh_token ON auth_sessions(refresh_token);
CREATE INDEX idx_auth_sessions_user_id ON auth_sessions(user_id);
