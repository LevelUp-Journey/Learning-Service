package com.levelupjourney.learningservice.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;

@RestController
@RequestMapping("/debug/jwt")
@Slf4j
public class DebugJwtController {

    @Value("${app.jwt.secret}")
    private String secret;

    @PostMapping("/verify")
    public String verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            
            // Mostrar el secret configurado
            log.info("SECRET CONFIGURADO (primeros 32 chars): {}", secret.substring(0, 32));
            log.info("SECRET CONFIGURADO (últimos 32 chars): {}", secret.substring(secret.length() - 32));
            log.info("SECRET LENGTH: {}", secret.length());
            
            // Intentar parsear
            byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return "✅ TOKEN VÁLIDO - UserId: " + claims.get("userId") + ", Roles: " + claims.get("roles");
            
        } catch (Exception e) {
            log.error("ERROR: {}", e.getMessage());
            return "❌ TOKEN INVÁLIDO: " + e.getMessage() + 
                   "\n\n⚠️ El token fue firmado con un SECRET DIFERENTE al configurado en este servicio." +
                   "\n\n🔧 ACCIÓN REQUERIDA: Genera un NUEVO token desde el Auth Service usando el secret: " +
                   "\nd3ee61e796a04895924719559f19a19de2e31d4c330208d8c410c2d63c89de9b5aadc298404a843d521569e812c599bda47b65d4c7fce5a8ded21f24d8df59dc";
        }
    }
}
