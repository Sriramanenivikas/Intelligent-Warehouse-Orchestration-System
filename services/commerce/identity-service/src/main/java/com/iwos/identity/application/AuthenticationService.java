package com.iwos.identity.application;

import com.iwos.identity.domain.auth.AuthenticatedPrincipal;
import com.iwos.identity.domain.auth.InvalidCredentialsException;
import com.iwos.identity.infrastructure.config.IdentityServiceProperties;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final Map<String, IdentityServiceProperties.DemoPrincipal> principalsByUsername;

    public AuthenticationService(IdentityServiceProperties properties) {
        this.principalsByUsername = properties.demoPrincipals().stream()
                .collect(Collectors.toUnmodifiableMap(
                        IdentityServiceProperties.DemoPrincipal::username,
                        Function.identity()
                ));
    }

    public AuthenticatedPrincipal authenticate(String username, String password) {
        IdentityServiceProperties.DemoPrincipal principal = principalsByUsername.get(username);
        if (principal == null || !principal.password().equals(password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return new AuthenticatedPrincipal(
                principal.subject(),
                principal.username(),
                principal.role(),
                principal.region(),
                principal.orgId(),
                principal.nodeIds()
        );
    }
}
