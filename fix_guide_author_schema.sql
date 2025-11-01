-- Migration to remove invalid author_id column from guides table
-- This column should not exist as authors are stored in the guide_authors junction table

-- Remove the NOT NULL constraint and then drop the column
ALTER TABLE guides DROP COLUMN IF EXISTS author_id CASCADE;

-- Verify the guide_authors table exists and has the correct structure
-- This is just for verification, no changes needed if table exists
-- CREATE TABLE IF NOT EXISTS guide_authors (
--     id BIGSERIAL PRIMARY KEY,
--     guide_id UUID NOT NULL REFERENCES guides(id) ON DELETE CASCADE,
--     author_id VARCHAR(255) NOT NULL,
--     UNIQUE(guide_id, author_id)
-- );
