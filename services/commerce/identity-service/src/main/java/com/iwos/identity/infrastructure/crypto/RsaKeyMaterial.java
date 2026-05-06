package com.iwos.identity.infrastructure.crypto;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.UUID;

public record RsaKeyMaterial(
        String keyId,
        RSAPublicKey publicKey,
        RSAPrivateKey privateKey,
        String modulus,
        String exponent
) {

    public static RsaKeyMaterial generate() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RsaKeyMaterial(
                    "iwos-" + UUID.randomUUID(),
                    publicKey,
                    privateKey,
                    encode(publicKey.getModulus()),
                    encode(publicKey.getPublicExponent())
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("RSA algorithm is not available", exception);
        }
    }

    private static String encode(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
