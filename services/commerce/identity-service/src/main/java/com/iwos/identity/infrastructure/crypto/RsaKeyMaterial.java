package com.iwos.identity.infrastructure.crypto;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.core.io.Resource;

public record RsaKeyMaterial(
        String keyId,
        RSAPublicKey publicKey,
        RSAPrivateKey privateKey,
        String modulus,
        String exponent
) {

    public static RsaKeyMaterial load(String keyId, Resource privateKeyResource, Resource publicKeyResource) {
        try {
            RSAPrivateKey privateKey = (RSAPrivateKey) readPrivateKey(privateKeyResource);
            RSAPublicKey publicKey = (RSAPublicKey) readPublicKey(publicKeyResource);
            return new RsaKeyMaterial(
                    keyId,
                    publicKey,
                    privateKey,
                    encode(publicKey.getModulus()),
                    encode(publicKey.getPublicExponent())
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load RSA key material", exception);
        }
    }

    private static PublicKey readPublicKey(Resource resource) throws Exception {
        String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String content = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    private static PrivateKey readPrivateKey(Resource resource) throws Exception {
        String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String content = pem
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] pkcs1 = Base64.getDecoder().decode(content);
        byte[] pkcs8 = wrapPkcs1InPkcs8(pkcs1);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
    }

    private static byte[] wrapPkcs1InPkcs8(byte[] pkcs1) {
        byte[] header = new byte[]{
                0x30, (byte) 0x82, 0x00, 0x00,
                0x02, 0x01, 0x00,
                0x30, 0x0d,
                0x06, 0x09,
                0x2a, (byte) 0x86, 0x48, (byte) 0x86,
                (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01,
                0x05, 0x00,
                0x04, (byte) 0x82, 0x00, 0x00
        };
        int totalLength = header.length - 4 + pkcs1.length;
        header[2] = (byte) ((totalLength >> 8) & 0xff);
        header[3] = (byte) (totalLength & 0xff);
        int octetLength = pkcs1.length;
        header[24] = (byte) ((octetLength >> 8) & 0xff);
        header[25] = (byte) (octetLength & 0xff);
        byte[] pkcs8 = new byte[header.length + pkcs1.length];
        System.arraycopy(header, 0, pkcs8, 0, header.length);
        System.arraycopy(pkcs1, 0, pkcs8, header.length, pkcs1.length);
        return pkcs8;
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
