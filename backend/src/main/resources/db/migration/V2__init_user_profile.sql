-- =============================================
-- V2: User profile and blocks
-- =============================================

-- user_profiles: user display info (1:1 with users)
CREATE TABLE user_profiles (
    user_id              BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    display_name         VARCHAR(50)  NOT NULL,
    avatar_url           VARCHAR(512),                        -- MinIO URL or DiceBear default
    bio                  VARCHAR(500),
    privacy_level        VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',  -- PUBLIC | FRIENDS | PRIVATE
    notif_email_enabled  BOOLEAN      NOT NULL DEFAULT TRUE,
    notif_push_enabled   BOOLEAN      NOT NULL DEFAULT TRUE,
    timezone             VARCHAR(50)  NOT NULL DEFAULT 'Asia/Shanghai',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_profile_privacy CHECK (privacy_level IN ('PUBLIC', 'FRIENDS', 'PRIVATE'))
);

-- user_blocks: one-directional block relationship
CREATE TABLE user_blocks (
    id           BIGSERIAL PRIMARY KEY,
    blocker_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(blocker_id, blocked_id),
    CHECK(blocker_id <> blocked_id)
);

CREATE INDEX idx_blocks_blocker ON user_blocks(blocker_id);
CREATE INDEX idx_blocks_blocked ON user_blocks(blocked_id);
