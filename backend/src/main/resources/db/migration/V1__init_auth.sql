-- =============================================
-- V1: Auth tables (users, email_verifications)
-- =============================================

-- users: account (authentication subject)
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,               -- Argon2id hash
    email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | SUSPENDED | DELETED
    role                VARCHAR(20)  NOT NULL DEFAULT 'USER',    -- USER | ADMIN
    failed_login_count  INT          NOT NULL DEFAULT 0,
    locked_until        TIMESTAMPTZ,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),
    CONSTRAINT chk_users_role   CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role   ON users(role);

-- email_verifications: tokens for registration, email change, password reset
CREATE TABLE email_verifications (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,              -- SHA-256 hash of the raw token
    purpose      VARCHAR(20)  NOT NULL,                      -- REGISTER | EMAIL_CHANGE | PASSWORD_RESET
    expires_at   TIMESTAMPTZ  NOT NULL,
    used_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_email_verif_purpose CHECK (purpose IN ('REGISTER', 'EMAIL_CHANGE', 'PASSWORD_RESET'))
);

CREATE INDEX idx_email_verif_user  ON email_verifications(user_id);
CREATE INDEX idx_email_verif_token ON email_verifications(token_hash);
CREATE INDEX idx_email_verif_purpose ON email_verifications(purpose, expires_at);
