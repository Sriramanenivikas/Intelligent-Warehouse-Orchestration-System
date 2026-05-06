package com.iwos.identity.api.http;

import com.iwos.identity.application.AuthenticationService;
import com.iwos.identity.application.TokenIssuerService;
import com.iwos.identity.domain.auth.AuthenticatedPrincipal;
import com.iwos.identity.infrastructure.crypto.JwksResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthController {

    private final AuthenticationService authenticationService;
    private final TokenIssuerService tokenIssuerService;
    private final JwksResponseFactory jwksResponseFactory;

    public AuthController(
            AuthenticationService authenticationService,
            TokenIssuerService tokenIssuerService,
            JwksResponseFactory jwksResponseFactory
    ) {
        this.authenticationService = authenticationService;
        this.tokenIssuerService = tokenIssuerService;
        this.jwksResponseFactory = jwksResponseFactory;
    }

    @PostMapping("/api/v1/auth/token")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse issueToken(
            @Valid @RequestBody TokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthenticatedPrincipal principal = authenticationService.authenticate(request.username(), request.password());
        return tokenIssuerService.issue(principal, request.requestedNodeId(), Instant.now(), httpServletRequest.getRequestURI());
    }

    @GetMapping("/.well-known/jwks.json")
    public Object jwks() {
        return jwksResponseFactory.create();
    }
}
