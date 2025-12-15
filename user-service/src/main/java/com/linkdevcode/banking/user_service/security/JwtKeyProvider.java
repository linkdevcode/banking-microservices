package com.linkdevcode.banking.user_service.security;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.security.PrivateKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;

import java.util.Base64;

@Component
public class JwtKeyProvider {

    @Value("classpath:jwt/private.pem")
    private Resource privateKeyResource;

    public PrivateKey getPrivateKey() {
        try {
            String key = new String(privateKeyResource.getInputStream().readAllBytes())
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load private key", e);
        }
    }
}
