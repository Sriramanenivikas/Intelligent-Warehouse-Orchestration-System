package com.iwos.taskexecution.api.http;

import jakarta.validation.constraints.NotBlank;

public record CompleteTaskRequest(
        @NotBlank(message = "workerId is required")
        String workerId,
        String notes
) {}
