-- V10: Add grace deadline columns for focus session timeout management
-- grace_deadline: 宽限期截止时间（非null表示处于宽限期）
-- grace_reason: 宽限期原因（LEARNING_LIMIT=达到8h学习上限, PAUSE_LIMIT=暂停超过1h）
ALTER TABLE focus_sessions ADD COLUMN grace_deadline TIMESTAMPTZ;
ALTER TABLE focus_sessions ADD COLUMN grace_reason VARCHAR(20);
