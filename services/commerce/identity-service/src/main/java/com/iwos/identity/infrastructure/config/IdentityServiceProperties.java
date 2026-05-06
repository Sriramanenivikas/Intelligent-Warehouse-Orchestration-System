package com.iwos.identity.infrastructure.config;

import com.iwos.identity.domain.auth.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "identity-service")
public record IdentityServiceProperties(
        @NotBlank String issuer,
        @NotBlank String audience,
        @NotNull Duration tokenTtl,
        @NotBlank String keyId,
        @NotNull Resource privateKeyLocation,
        @NotNull Resource publicKeyLocation,
        @Valid @NotEmpty List<DemoPrincipal> demoPrincipals
) {

    public record DemoPrincipal(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String subject,
            @NotNull Role role,
            @NotBlank String region,
            @NotBlank String orgId,
            @NotEmpty List<String> nodeIds
    ) {
    }
}
