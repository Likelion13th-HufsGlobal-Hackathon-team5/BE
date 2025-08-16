package com.hackathon_5.Yogiyong_In.config;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecretKey key;

    @Value("${jwt.issuer:app}")
    private String issuer;

    @Value("${jwt.access-token-seconds:3600}")
    private long accessTokenSeconds;

    public String createToken(String subject, Map<String, String> claims) {

        return generateToken(subject, (Map) claims, accessTokenSeconds);
    }

    public String generateToken(String subject) {
        return generateToken(subject, Map.of(), accessTokenSeconds);
    }

    public String generateToken(String subject, Map<String, Object> claims, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public SecretKey getKey() {
        return key;
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenSeconds;
    }
}