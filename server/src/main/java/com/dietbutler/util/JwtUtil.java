package com.dietbutler.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:your-secret-key-should-be-at-least-32-characters-long}")
    private String secret;

    @Value("${jwt.expire-days:7}")
    private int expireDays;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成无状态JWT Token
     */
    public String generateToken(Long userId, String openid) {
        Date expireDate = new Date(System.currentTimeMillis() + expireDays * 24 * 60 * 60 * 1000L);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("openid", openid)
                .claim("isGuest", false)  // 微信登录用户都不是游客
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return !claims.getExpiration().before(new Date());
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return Long.parseLong(claims.getSubject());
    }

    public String getOpenidFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("openid", String.class);
    }

    public boolean isGuestFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;  // 默认返回false，因为已无游客登录
        }
        Boolean isGuest = claims.get("isGuest", Boolean.class);
        return isGuest != null && isGuest;
    }
}