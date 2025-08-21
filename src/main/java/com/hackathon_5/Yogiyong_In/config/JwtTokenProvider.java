package com.hackathon_5.Yogiyong_In.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
@Getter
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValiditySeconds;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-validity-seconds}") long accessTokenValiditySeconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public String createToken(String subject, Map<String, Object> claims, long validitySeconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validitySeconds * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createToken(Authentication authentication) {
        String subject = authentication.getName();
        return createToken(subject, Map.of(), accessTokenValiditySeconds);
    }

    public String createToken(String subject, Map<String, Object> claims) {
        return createToken(subject, claims, accessTokenValiditySeconds);
    }

    public String createCheckToken(String value, String type, long validitySeconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validitySeconds * 1000);

        return Jwts.builder()
                .claim(type, value)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }
}
