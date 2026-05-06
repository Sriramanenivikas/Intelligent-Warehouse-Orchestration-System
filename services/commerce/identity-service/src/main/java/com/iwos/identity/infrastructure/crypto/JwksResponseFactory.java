package com.iwos.identity.infrastructure.crypto;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JwksResponseFactory {

    private final RsaKeyMaterial rsaKeyMaterial;

    public JwksResponseFactory(RsaKeyMaterial rsaKeyMaterial) {
        this.rsaKeyMaterial = rsaKeyMaterial;
    }

    public Map<String, Object> create() {
        return Map.of(
                "keys", List.of(Map.of(
                        "kty", "RSA",
                        "kid", rsaKeyMaterial.keyId(),
                        "use", "sig",
                        "alg", "RS256",
                        "n", rsaKeyMaterial.modulus(),
                        "e", rsaKeyMaterial.exponent()
                ))
        );
    }
}
