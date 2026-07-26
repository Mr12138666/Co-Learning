-- Add scheduling and planning fields to study_tasks
ALTER TABLE study_tasks ADD COLUMN IF NOT EXISTS planned_date DATE;
ALTER TABLE study_tasks ADD COLUMN IF NOT EXISTS scheduled_start TIMESTAMP WITH TIME ZONE;
ALTER TABLE study_tasks ADD COLUMN IF NOT EXISTS scheduled_end TIMESTAMP WITH TIME ZONE;
ALTER TABLE study_tasks ADD COLUMN IF NOT EXISTS estimated_minutes INTEGER;
ALTER TABLE study_tasks ADD COLUMN IF NOT EXISTS urgent BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE study_tasks ADD COLUMN IF NOT EXISTS important BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for planned_date queries (Inbox: tasks without planned_date, Planner: tasks by date)
CREATE INDEX IF NOT EXISTS idx_study_tasks_planned_date ON study_tasks(user_id, planned_date);
CREATE INDEX IF NOT EXISTS idx_study_tasks_status ON study_tasks(user_id, status);
