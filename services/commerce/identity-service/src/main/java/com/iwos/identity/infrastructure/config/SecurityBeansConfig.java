package com.iwos.identity.infrastructure.config;

import com.iwos.identity.infrastructure.crypto.RsaKeyMaterial;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public RsaKeyMaterial rsaKeyMaterial(IdentityServiceProperties properties) {
        return RsaKeyMaterial.load(
                properties.keyId(),
                properties.privateKeyLocation(),
                properties.publicKeyLocation()
        );
    }
}
