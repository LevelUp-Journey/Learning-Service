package com.levelupjourney.learningservice.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        // Convertir el string a bytes UTF-8 y crear la clave HMAC-SHA
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Valida el JWT y extrae los claims.
     * La función principal es validar los roles del usuario y pasar el ID del usuario al request.
     * @param token El token JWT Bearer
     * @return Los claims del token si es válido
     * @throws Exception Si el token es inválido o faltan campos requeridos
     */
    public Claims validateAndGetClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Validar que el token no haya expirado
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                throw new RuntimeException("Token has expired");
            }

            // Validar que tenga userId
            String userId = claims.get("userId", String.class);
            if (userId == null || userId.trim().isEmpty()) {
                throw new RuntimeException("Token missing userId");
            }

            // Validar que tenga roles
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            if (roles == null || roles.isEmpty()) {
                throw new RuntimeException("Token missing roles");
            }

            log.info("Token valid - Subject: {}, UserId: {}, Roles: {}", 
                    claims.getSubject(), userId, roles);
            
            return claims;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extrae el ID del usuario de los claims.
     * @param claims Los claims del token
     * @return El ID del usuario
     */
    public String getUserId(Claims claims) {
        return claims.get("userId", String.class);
    }

    /**
     * Extrae los roles del usuario de los claims.
     * @param claims Los claims del token
     * @return La lista de roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        return claims.get("roles", List.class);
    }
}

