ALTER TABLE task_execution.task_assignments
    DROP CONSTRAINT IF EXISTS chk_status;

ALTER TABLE task_execution.task_assignments
    ADD CONSTRAINT chk_status CHECK (status IN ('READY', 'BLOCKED', 'CLAIMED', 'IN_PROGRESS', 'COMPLETED', 'FAILED'));
