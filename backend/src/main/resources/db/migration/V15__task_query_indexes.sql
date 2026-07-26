-- V15: Additional indexes for task query paths
-- Already existing (do NOT recreate):
--   V3:  idx_tasks_user(user_id), idx_tasks_subject(subject_id), idx_tasks_status_due(user_id, status, due_date)
--   V13: idx_study_tasks_planned_date(user_id, planned_date), idx_study_tasks_status(user_id, status)
--   V14: idx_task_tags_task_id(task_id), idx_task_tags_tag_id(tag_id)

-- Quadrant board queries: WHERE user_id = ? AND urgent = ? AND important = ?
CREATE INDEX IF NOT EXISTS idx_study_tasks_quadrant ON study_tasks(user_id, urgent, important);

-- Per-task focus time aggregation: WHERE task_id = ? AND status = 'FINISHED'
CREATE INDEX IF NOT EXISTS idx_focus_sessions_task ON focus_sessions(task_id) WHERE task_id IS NOT NULL;
