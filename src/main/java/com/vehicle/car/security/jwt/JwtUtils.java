package com.vehicle.car.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.vehicle.car.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs:1209600000}")
    private long jwtExpirationMs;
    
    @Value("${jwt.refreshExpirationMs:2592000000}")
    private long refreshTokenExpirationMs;
    
    @Value("${cookie.secure:false}")
    private boolean cookieSecure;
    
    @Value("${cookie.httpOnly:true}")
    private boolean cookieHttpOnly;
    
    @Value("${cookie.sameSite:Lax}")
    private String cookieSameSite;
    
    public static final String JWT_COOKIE_NAME = "servispilot_jwt";
    private static final String REFRESH_COOKIE_NAME = "servispilot_refresh";

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }
    
    public String generateTokenFromUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String generateRefreshToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateRefreshTokenFromUsername(userPrincipal.getUsername());
    }
    
    public String generateRefreshTokenFromUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + refreshTokenExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // JWT token'ı çerez (cookie) olarak oluşturur
    public ResponseCookie generateJwtCookie(Authentication authentication) {
        String jwt = generateJwtToken(authentication);
        return ResponseCookie.from(JWT_COOKIE_NAME, jwt)
                .path("/")
                .maxAge(jwtExpirationMs / 1000) // Saniye cinsinden süre
                .httpOnly(cookieHttpOnly)  // JavaScript erişimini engelle
                .secure(cookieSecure)   // HTTP üzerinden de gönderilir
                .sameSite(cookieSameSite) // Daha esnek CSRF koruması
                .build();
    }
    
    // Refresh token'ı çerez (cookie) olarak oluşturur
    public ResponseCookie generateRefreshCookie(Authentication authentication) {
        String refreshToken = generateRefreshToken(authentication);
        return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .path("/")
                .maxAge(refreshTokenExpirationMs / 1000) // Saniye cinsinden süre
                .httpOnly(cookieHttpOnly)  // JavaScript erişimini engelle
                .secure(cookieSecure)   // HTTP üzerinden de gönderilir
                .sameSite(cookieSameSite) // Daha esnek CSRF koruması
                .build();
    }
    
    // Temizlenmek için boş çerez oluşturur
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(JWT_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();
    }
    
    // Temizlenmek için boş refresh çerez oluşturur
    public ResponseCookie getCleanRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
    
    // HTTP request'ten JWT token'ı alır (header veya cookie'den)
    public String getJwtFromRequest(HttpServletRequest request) {
        // Önce header'dan kontrol et
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        
        // Sonra cookie'den kontrol et
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
    
    // HTTP request'ten Refresh token'ı alır
    public String getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    // Token tipini kontrol eder (access veya refresh)
    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
            return "access".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
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

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
    
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
} 