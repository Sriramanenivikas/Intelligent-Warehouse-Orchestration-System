package com.iwos.taskexecution.domain.task;

import java.util.UUID;

public class TaskAlreadyClaimedException extends RuntimeException {

    private final UUID taskId;
    private final String claimedByWorkerId;

    public TaskAlreadyClaimedException(UUID taskId, String claimedByWorkerId) {
        super(String.format("Task %s already claimed by worker %s", taskId, claimedByWorkerId));
        this.taskId = taskId;
        this.claimedByWorkerId = claimedByWorkerId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getClaimedByWorkerId() {
        return claimedByWorkerId;
    }
}
