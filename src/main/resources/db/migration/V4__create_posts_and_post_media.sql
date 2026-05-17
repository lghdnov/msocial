CREATE TABLE IF NOT EXISTS posts (
    id                      BIGSERIAL PRIMARY KEY,
    author_id               BIGINT NOT NULL,
    author_name             VARCHAR(255) NOT NULL,
    content                 VARCHAR(5000) NOT NULL,
    deleted                 BOOLEAN NOT NULL DEFAULT false,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    version                 BIGINT DEFAULT 0,
    CONSTRAINT fk_posts_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS post_media (
    id                      BIGSERIAL PRIMARY KEY,
    post_id                 BIGINT NOT NULL,
    url                     VARCHAR(500) NOT NULL,
    sort_order              INTEGER NOT NULL,
    version                 BIGINT DEFAULT 0,
    CONSTRAINT fk_post_media_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_post_media_post_id ON post_media(post_id);
