package com.hackathon_5.Yogiyong_In.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtKeyConfig {

    @Value("${jwt.secret}")   // Base64 인코딩된 32바이트(이상) 시크릿
    private String secret;

    @Bean
    public SecretKey jwtSigningKey() {
        // JJWT 0.11.x 권장 방식: Base64 디코드 → HMAC 키 생성
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
