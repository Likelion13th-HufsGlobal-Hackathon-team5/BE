package com.hackathon_5.Yogiyong_In.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtKeyConfig {

    @Bean
    public SecretKey jwtSigningKey(@Value("${jwt.secret}") String base64Secret) {

        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }
}
