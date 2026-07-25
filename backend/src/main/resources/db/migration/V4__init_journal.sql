-- V4: Journal module - private journals/diary with soft delete

-- journals: 私密日志/日记(软删除)
-- Note: room_id FK to rooms(id) will be added in V5 when rooms table is created
CREATE TABLE journals (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title             VARCHAR(200),
    content_markdown  TEXT NOT NULL,
    content_html      TEXT,
    visibility        VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    room_id           BIGINT,
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at      TIMESTAMPTZ,
    ai_summary        TEXT,
    deleted_at        TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_journals_user ON journals(user_id, deleted_at);
CREATE INDEX idx_journals_visibility ON journals(visibility, status, deleted_at);
CREATE INDEX idx_journals_user_status ON journals(user_id, status, deleted_at);
