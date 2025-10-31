package com.levelupjourney.learningservice.shared.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test utility for generating JWT tokens with specific roles
 * Uses the same signing method as JwtTokenProvider for consistency
 */
@Component
public class TestJwtTokenProvider {
    
    private final SecretKey SECRET_KEY;
    private static final long EXPIRATION_TIME = 86400000; // 24 hours
    
    public TestJwtTokenProvider(@Value("${jwt.secret}") String secret) {
        // Use BASE64 decoding like JwtTokenProvider does
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(String userId, String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }
    
    public String generateStudentToken(String userId) {
        return generateToken(userId, "student" + userId, List.of("ROLE_STUDENT"));
    }
    
    public String generateTeacherToken(String userId) {
        return generateToken(userId, "teacher" + userId, List.of("ROLE_TEACHER"));
    }
    
    public String generateAdminToken(String userId) {
        return generateToken(userId, "admin" + userId, List.of("ROLE_ADMIN"));
    }
}
