package com.iwos.identity.application;

import com.iwos.identity.api.http.TokenResponse;
import com.iwos.identity.domain.auth.AuthenticatedPrincipal;
import com.iwos.identity.domain.auth.PermissionCatalog;
import com.iwos.identity.domain.auth.Role;
import com.iwos.identity.infrastructure.config.IdentityServiceProperties;
import com.iwos.identity.infrastructure.crypto.RsaKeyMaterial;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.iwos.identity.shared.RequestContextFilter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TokenIssuerService {

    private final IdentityServiceProperties properties;
    private final RsaKeyMaterial rsaKeyMaterial;

    public TokenIssuerService(
            IdentityServiceProperties properties,
            RsaKeyMaterial rsaKeyMaterial
    ) {
        this.properties = properties;
        this.rsaKeyMaterial = rsaKeyMaterial;
    }

    public TokenResponse issue(
            AuthenticatedPrincipal principal,
            String requestedNodeId,
            Instant issuedAt,
            String requestPath
    ) {
        List<String> nodeIds = requestedNodeId == null || requestedNodeId.isBlank()
                ? principal.nodeIds()
                : principal.nodeIds().stream().filter(requestedNodeId::equals).toList();
        if (nodeIds.isEmpty()) {
            nodeIds = principal.nodeIds();
        }
        Role role = principal.role();
        List<String> permissions = PermissionCatalog.permissionsFor(role);
        Instant expiresAt = issuedAt.plus(properties.tokenTtl());
        String token = signToken(principal, role, permissions, nodeIds, issuedAt, expiresAt, requestPath);
        return new TokenResponse(
                "Bearer",
                token,
                properties.issuer(),
                properties.audience(),
                principal.subject(),
                principal.username(),
                role.name(),
                permissions,
                nodeIds,
                principal.region(),
                principal.orgId(),
                RequestContextFilter.currentRequestId(),
                issuedAt,
                expiresAt
        );
    }

    private String signToken(
            AuthenticatedPrincipal principal,
            Role role,
            List<String> permissions,
            List<String> nodeIds,
            Instant issuedAt,
            Instant expiresAt,
            String requestPath
    ) {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(properties.issuer())
                .audience(List.of(properties.audience()))
                .subject(principal.subject())
                .issueTime(java.util.Date.from(issuedAt))
                .expirationTime(java.util.Date.from(expiresAt))
                .jwtID(UUID.randomUUID().toString())
                .claim("username", principal.username())
                .claim("role", role.name())
                .claim("permissions", permissions)
                .claim("nodeIds", nodeIds)
                .claim("region", principal.region())
                .claim("orgId", principal.orgId())
                .claim("requestPath", requestPath)
                .build();
        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(rsaKeyMaterial.keyId())
                        .type(JOSEObjectType.JWT)
                        .build(),
                claimsSet
        );
        try {
            signedJwt.sign(new RSASSASigner(rsaKeyMaterial.privateKey()));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Failed to sign JWT", exception);
        }
    }
}
