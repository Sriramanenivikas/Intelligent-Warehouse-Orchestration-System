package com.iwos.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private String userId;
    private String email;
    private Set<String> roles;
    private String tenantId;
}
