package com.residence.repair.security;


import com.residence.repair.domain.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Outil JWT: génération et validation des access tokens.
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpireTime;

    private SecretKey key;

    /**
     * Initialisation de la clé secrète.
     */
    @PostConstruct
    public void init() {
        //HS512 nécessite une clé suffisamment longue (>= 64 bytes).
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Générer Access Token.
     */
    public String generateAccessToken(String email, UserRole role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", "ACCESS");
        return generateToken(claims, email, accessTokenExpireTime);
    }

    private String generateToken(Map<String, Object> claims,
                                 String subject,
                                 long expireTime) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) //email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extraire email depuis token.
     */
    public String getUsernameFromToken(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extraire role depuis token.
     */
    public String getRoleFromToken(String token) {
        return extractClaims(token).get("role").toString();
    }

    /**
     * Valider token.
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }
    /**
     * Extraire claims (payload) depuis token.
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
