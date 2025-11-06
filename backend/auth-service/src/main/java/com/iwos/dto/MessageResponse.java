package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic Message Response DTO
 * Used for simple API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;
    private boolean success;

    public static MessageResponse of(String message) {
        return MessageResponse.builder()
                .message(message)
                .success(true)
                .build();
    }

    public static MessageResponse error(String message) {
        return MessageResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
}
