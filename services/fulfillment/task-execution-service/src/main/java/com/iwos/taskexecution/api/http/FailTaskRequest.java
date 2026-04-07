package com.iwos.taskexecution.api.http;

import jakarta.validation.constraints.NotBlank;

public record FailTaskRequest(
        @NotBlank(message = "workerId is required")
        String workerId,
        @NotBlank(message = "reason is required")
        String reason
) {}
