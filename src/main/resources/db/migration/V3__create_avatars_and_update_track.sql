CREATE TABLE IF NOT EXISTS avatars (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    url                     VARCHAR(500) NOT NULL,
    uploaded_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    active                  BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_avatars_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_avatars_user_id ON avatars(user_id);

ALTER TABLE personal_info
    RENAME COLUMN favorite_track TO favorite_track_url;

ALTER TABLE personal_info
    ALTER COLUMN favorite_track_url TYPE VARCHAR(500);
