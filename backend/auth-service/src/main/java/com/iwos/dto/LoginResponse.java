package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Response DTO
 * Contains JWT tokens and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // in seconds
    private String tokenType;
    private UserInfo user;

    public static LoginResponse of(String accessToken, String refreshToken, Long expiresIn, UserInfo user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .user(user)
                .build();
    }
}
