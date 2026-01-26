package com.celebrationpoint.backend.config;

import com.celebrationpoint.backend.entity.User;
import com.celebrationpoint.backend.entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    /**
     * 🔐 BASE64 ENCODED 256-bit SECRET
     * (DO NOT use plain text for HS256)
     */
    private static final String SECRET_KEY =
            "c2VsZWJyYXRpb25wb2ludF9zdXBlcl9zZWNyZXRfa2V5XzI1Ng==";

    // ✅ ALWAYS decode Base64
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ GENERATE TOKEN (USER ENTITY)
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        String role = user.getRoles()
                .stream()
                .map(Role::getName)
                .map(Enum::name)
                .findFirst()
                .orElse("ROLE_USER");

        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)
                ) // 24 hours
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔍 EXTRACT EMAIL
    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception e) {
            // 🔥 MALFORMED TOKEN (invalid format/signature)
            return null;
        }
    }

    // 🔍 EXTRACT ROLE
    public String extractRole(String token) {
        try {
            return extractAllClaims(token).get("role", String.class);
        } catch (Exception e) {
            // 🔥 MALFORMED TOKEN
            return null;
        }
    }

    // 🔐 VALIDATE TOKEN
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            // 🔥 MALFORMED TOKEN or EXPIRED
            return false;
        }
    }

    // ⏰ EXPIRY CHECK
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // 🔐 CLAIM PARSER (SIGN + VERIFY USE SAME KEY)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
