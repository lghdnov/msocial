CREATE TABLE IF NOT EXISTS users (
    id                      BIGSERIAL PRIMARY KEY,
    external_id             VARCHAR(255) NOT NULL UNIQUE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    version                 BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS personal_info (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL UNIQUE,
    birth_date              DATE,
    address                 VARCHAR(500),
    favorite_track          VARCHAR(255),
    status                  VARCHAR(255),
    version                 BIGINT DEFAULT 0,
    CONSTRAINT fk_personal_info_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_users_external_id ON users(external_id);
CREATE INDEX idx_personal_info_user_id ON personal_info(user_id);
