-- ============================================================
-- Migration: Split Question Type into Separate Lookup Table
-- Description: Replace questions.type ENUM with questions.type_id FK
-- Run this against the question_db database
-- ============================================================

-- Step 1: Create the question_types lookup table
CREATE TABLE IF NOT EXISTS question_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '类型标识(single/multiple/judge/fill/essay)',
    display_name VARCHAR(50) NOT NULL COMMENT '显示名称(单选题/多选题/判断题/填空题/问答题)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目类型字典表';

-- Step 2: Seed the 5 type values
INSERT INTO question_types (name, display_name) VALUES
('single', '单选题'),
('multiple', '多选题'),
('judge', '判断题'),
('fill', '填空题'),
('essay', '问答题');

-- Step 3: Add nullable type_id column
ALTER TABLE questions ADD COLUMN type_id BIGINT NULL AFTER content;

-- Step 4: Populate type_id by joining on the old ENUM value = lookup name
UPDATE questions q
JOIN question_types qt ON q.type = qt.name
SET q.type_id = qt.id;

-- Step 5: Verify no NULLs remain (should return 0)
-- SELECT COUNT(*) FROM questions WHERE type_id IS NULL;

-- Step 6: Make it NOT NULL
ALTER TABLE questions MODIFY COLUMN type_id BIGINT NOT NULL;

-- Step 7: Add foreign key constraint
ALTER TABLE questions ADD CONSTRAINT fk_question_type
    FOREIGN KEY (type_id) REFERENCES question_types(id)
    ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Step 8: Add index on FK column
ALTER TABLE questions ADD INDEX idx_type_id (type_id);

-- Step 9: Drop old type column and its index
ALTER TABLE questions DROP INDEX idx_type;
ALTER TABLE questions DROP COLUMN type;
