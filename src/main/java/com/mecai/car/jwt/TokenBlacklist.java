package com.mecai.car.jwt;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Lista de tokens revocados (logout), identificados por su jti.
 * Está en memoria: se pierde al reiniciar y no se comparte entre instancias.
 * Cuando haya BD o Redis, solo cambia esta clase.
 */
@Component
public class TokenBlacklist {
    // jti -> fecha de expiración del token (pasada esa fecha ya no hace falta recordarlo)
    private final Map<String, Date> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, Date expiration) {
        purgeExpired();
        revoked.put(jti, expiration);
    }

    public boolean isRevoked(String jti) {
        return revoked.containsKey(jti);
    }

    private void purgeExpired() {
        Date now = new Date();
        revoked.values().removeIf(expiration -> expiration.before(now));
    }
}
