-- V7: Daily tasks system

CREATE TABLE daily_tasks (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_type           VARCHAR(50) NOT NULL,
    title               VARCHAR(100) NOT NULL,
    description         VARCHAR(500),
    target_value        INT NOT NULL,
    current_progress    INT NOT NULL DEFAULT 0,
    reward_tokens       INT NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    task_date           DATE NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_daily_tasks_user_date ON daily_tasks(user_id, task_date);
CREATE INDEX idx_daily_tasks_type ON daily_tasks(task_type);