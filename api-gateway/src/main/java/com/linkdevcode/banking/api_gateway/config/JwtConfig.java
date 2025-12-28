package com.linkdevcode.banking.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() throws Exception {
        System.out.println("🔥🔥🔥 LOADING CUSTOM JWT DECODER 🔥🔥🔥");
        InputStream is = new ClassPathResource("jwt/public.pem").getInputStream();
        String key = new String(is.readAllBytes());

        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(
                        Base64.getDecoder()
                                .decode(key
                                        .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                                        .replaceAll("\\s", ""))
                ));

        return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
    }
}