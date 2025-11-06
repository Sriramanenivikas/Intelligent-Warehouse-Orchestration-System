package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User Information DTO
 * Contains user details returned in responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private List<String> roles;
}
