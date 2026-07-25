-- V3: Study module - exam goals, subjects, tasks, focus sessions, daily checkins

-- exam_goals: 考试目标
CREATE TABLE exam_goals (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exam_name    VARCHAR(100) NOT NULL,
    exam_date    DATE NOT NULL,
    target_score VARCHAR(50),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_exam_goals_user ON exam_goals(user_id);

-- subjects: 科目
CREATE TABLE subjects (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name         VARCHAR(50) NOT NULL,
    color        VARCHAR(7)  NOT NULL DEFAULT '#2080F0',
    sort_order   INT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, name)
);

-- study_tasks: 学习任务
CREATE TABLE study_tasks (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id   BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    exam_goal_id BIGINT REFERENCES exam_goals(id) ON DELETE SET NULL,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    status       VARCHAR(20) NOT NULL DEFAULT 'TODO',
    due_date     DATE,
    sort_order   INT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tasks_user ON study_tasks(user_id);
CREATE INDEX idx_tasks_subject ON study_tasks(subject_id);
CREATE INDEX idx_tasks_status_due ON study_tasks(user_id, status, due_date);

-- focus_sessions: 学习会话(服务端权威)
CREATE TABLE focus_sessions (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id         BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    task_id            BIGINT REFERENCES study_tasks(id) ON DELETE SET NULL,
    status             VARCHAR(20) NOT NULL,
    started_at         TIMESTAMPTZ NOT NULL,
    paused_at          TIMESTAMPTZ,
    resumed_at         TIMESTAMPTZ,
    ended_at           TIMESTAMPTZ,
    paused_seconds     INT NOT NULL DEFAULT 0,
    effective_seconds  INT NOT NULL DEFAULT 0,
    client_request_id  VARCHAR(64),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_focus_sessions_user_status ON focus_sessions(user_id, status);
CREATE INDEX idx_focus_sessions_started ON focus_sessions(user_id, started_at DESC);
CREATE UNIQUE INDEX idx_focus_sessions_client_req ON focus_sessions(user_id, client_request_id)
    WHERE client_request_id IS NOT NULL;

-- daily_checkins: 每日打卡+复盘
CREATE TABLE daily_checkins (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    checkin_date    DATE NOT NULL,
    plan_text       VARCHAR(1000),
    reflection_text VARCHAR(2000),
    mood            SMALLINT,
    focus_total_sec INT NOT NULL DEFAULT 0,
    completed       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, checkin_date)
);
CREATE INDEX idx_checkins_date ON daily_checkins(user_id, checkin_date DESC);

-- daily_plans: 每日计划(可独立于 checkin)
CREATE TABLE daily_plans (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_date    DATE NOT NULL,
    content      TEXT NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, plan_date)
);
