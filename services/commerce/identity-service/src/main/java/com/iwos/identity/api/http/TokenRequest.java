package com.iwos.identity.api.http;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "username is required")
        String username,
        @NotBlank(message = "password is required")
        String password,
        String requestedNodeId
) {
}
