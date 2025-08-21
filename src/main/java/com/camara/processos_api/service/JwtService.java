package com.camara.processos_api.service; // Verifique se o seu pacote está correto

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private Key getSignInKey() {
        // Converte a chave de texto simples para bytes
        byte[] keyBytes = this.secretKey.getBytes(StandardCharsets.UTF_8);
        // Cria a chave usando a especificação HMAC-SHA, uma abordagem mais direta
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        try {
            Object uid = extractClaim(token, claims -> claims.get("uid"));
            if (uid == null) return null;
            if (uid instanceof Number) return ((Number) uid).longValue();
            return Long.parseLong(String.valueOf(uid));
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            String a = username == null ? null : username.trim();
            String b = userDetails.getUsername() == null ? null : userDetails.getUsername().trim();
            return (a != null && a.equals(b)) && !isTokenExpired(token);
        } catch (SignatureException e) {
            // Adiciona um log para vermos o erro de assinatura claramente
            System.err.println("[ERRO JWT] Assinatura do token é inválida: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}