package com.iwos.taskexecution.api.http;

import jakarta.validation.constraints.NotBlank;

public record ClaimTaskRequest(
        @NotBlank(message = "workerId is required")
        String workerId
) {}
