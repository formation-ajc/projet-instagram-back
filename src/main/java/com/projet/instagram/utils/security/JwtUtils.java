package com.projet.instagram.utils.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.projet.instagram.service.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

//import io.jsonwebtoken.*;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        return generateTokenFromEmail(userPrincipal.getEmail());
    }

    public String generateTokenFromEmail(String email) {

        SecretKeySpec secret_key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
//                .claim("name", "Micah Silverman")
//                .claim("scope", "admins")
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
//                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .signWith(secret_key)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        SecretKeySpec secret_key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

        return Jwts.parserBuilder().setSigningKey(secret_key).build().parseClaimsJws(token).getBody().getSubject();
//        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            SecretKeySpec secret_key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
//            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            Jwts.parserBuilder().setSigningKey(secret_key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

}
