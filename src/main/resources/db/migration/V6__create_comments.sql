CREATE TABLE comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    author_name VARCHAR(255) NOT NULL,
    parent_id BIGINT NULL,
    content VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_comments_post_id_status ON comments (post_id, status);
CREATE INDEX idx_comments_parent_id_status ON comments (parent_id, status);
CREATE INDEX idx_comments_created_at ON comments (created_at);
