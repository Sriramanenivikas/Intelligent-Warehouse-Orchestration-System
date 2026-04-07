package com.iwos.taskexecution.domain.task;

import java.util.UUID;

public class TaskStateException extends RuntimeException {

    private final UUID taskId;
    private final TaskStatus currentStatus;
    private final String attemptedAction;

    public TaskStateException(UUID taskId, TaskStatus currentStatus, String attemptedAction) {
        super(String.format("Cannot %s task %s in status %s", attemptedAction, taskId, currentStatus));
        this.taskId = taskId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public TaskStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}
