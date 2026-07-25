-- PostgreSQL init script
-- This runs once when the PostgreSQL container is first created.
-- Flyway handles all schema migrations, so this file only sets up
-- non-default server-level configuration if needed.

-- Enable the pg_trgm extension for potential fuzzy text search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Set timezone (data is stored as TIMESTAMPTZ/UTC, this is just the display default)
SET timezone TO 'UTC';
