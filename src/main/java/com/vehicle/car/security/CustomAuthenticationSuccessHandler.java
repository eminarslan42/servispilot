package com.vehicle.car.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.vehicle.car.security.jwt.JwtUtils;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        
        // JWT ve Refresh token oluştur ve cookie olarak ekle
        response.addHeader(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(authentication).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtUtils.generateRefreshCookie(authentication).toString());
        
        // Tarayıcı önbelleğini kontrol etmeye zorla (App Engine için)
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Admin rolüne sahip kullanıcıları admin/subscriptions sayfasına yönlendir
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                getRedirectStrategy().sendRedirect(request, response, "/admin/subscriptions");
                return;
            }
        }
        
        // Diğer kullanıcıları dashboard sayfasına yönlendir
        getRedirectStrategy().sendRedirect(request, response, "/dashboard");
    }
} 