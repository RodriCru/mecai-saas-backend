package com.mecai.car.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final TokenBlacklist tokenBlacklist;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId) {
        return generateToken(userId, accessTokenExpiration);
    }

    public String generateRefreshToken(UUID userId) {
        return generateToken(userId, refreshTokenExpiration);
    }

    // El subject es el id del usuario (único globalmente), no el email, que puede repetirse entre tenants
    private String generateToken(UUID userId, long expirationMillis) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token, String userId) {
        try {
            return !isTokenExpired(token)
                    && !tokenBlacklist.isRevoked(extractTokenId(token))
                    && extractUserId(token).equals(userId);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Logout: el token deja de ser válido aunque no haya expirado. Lanza JwtException si ya no es válido.
    public void revokeToken(String token) {
        tokenBlacklist.revoke(extractTokenId(token), extractExpiration(token));
    }

    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
