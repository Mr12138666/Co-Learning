-- V5: Room module - companion study rooms with real-time presence

-- rooms: study companion rooms (soft delete)
CREATE TABLE rooms (
    id              BIGSERIAL PRIMARY KEY,
    owner_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    max_members     INT NOT NULL DEFAULT 20,
    visibility      VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',  -- PUBLIC | PRIVATE
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | CLOSED
    password_hash   VARCHAR(255),                            -- for PRIVATE rooms
    topic           VARCHAR(200),                             -- room topic/subject
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_rooms_owner ON rooms(owner_id, deleted_at);
CREATE INDEX idx_rooms_visibility ON rooms(visibility, status, deleted_at);

-- room_members: who is in which room
CREATE TABLE room_members (
    id              BIGSERIAL PRIMARY KEY,
    room_id         BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL DEFAULT 'MEMBER',  -- OWNER | ADMIN | MEMBER
    muted_until     TIMESTAMPTZ,                             -- null = not muted
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    left_at         TIMESTAMPTZ,                             -- set when user leaves
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(room_id, user_id)                                 -- one membership per room per user
);
CREATE INDEX idx_room_members_room ON room_members(room_id, left_at);
CREATE INDEX idx_room_members_user ON room_members(user_id, left_at);

-- room_messages: chat messages in rooms (soft delete)
CREATE TABLE room_messages (
    id              BIGSERIAL PRIMARY KEY,
    room_id         BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         VARCHAR(500) NOT NULL,
    message_type    VARCHAR(20) NOT NULL DEFAULT 'TEXT',    -- TEXT | SYSTEM | FOCUS_STATUS
    focus_status    VARCHAR(20),                             -- STUDYING | PAUSED | IDLE (for FOCUS_STATUS type)
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_room_messages_room ON room_messages(room_id, created_at DESC);
CREATE INDEX idx_room_messages_user ON room_messages(user_id, deleted_at);

-- Add FK from journals.room_id to rooms(id) (deferred from V4)
ALTER TABLE journals
    ADD CONSTRAINT fk_journals_room
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL;
