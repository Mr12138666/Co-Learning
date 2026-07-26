-- Add daily focus goal minutes to user profiles
ALTER TABLE user_profiles
    ADD COLUMN daily_focus_goal_minutes INTEGER NOT NULL DEFAULT 120;