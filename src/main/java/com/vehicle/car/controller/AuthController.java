package com.vehicle.car.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vehicle.car.model.Role;
import com.vehicle.car.model.User;
import com.vehicle.car.payload.request.LoginRequest;
import com.vehicle.car.payload.request.PasswordResetRequest;
import com.vehicle.car.payload.request.SignupRequest;
import com.vehicle.car.payload.response.JwtResponse;
import com.vehicle.car.payload.response.MessageResponse;
import com.vehicle.car.payload.response.PasswordResetResponse;
import com.vehicle.car.repository.UserRepository;
import com.vehicle.car.security.jwt.JwtUtils;
import com.vehicle.car.security.services.UserDetailsImpl;
import com.vehicle.car.security.services.UserDetailsServiceImpl;
import com.vehicle.car.service.SubscriptionService;
import com.vehicle.car.service.UserService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // JWT Token oluştur
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // JWT Token'ı cookie olarak ekle
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(authentication);
        
        // Refresh Token'ı cookie olarak ekle
        ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Subscription kontrolü
        if (!subscriptionService.isSubscriptionValid(userDetails.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Aboneliğiniz bitti, lütfen servispilot@hotmail.com ile iletişime geçiniz."));
        }

        // Cookie'leri header'a ekle ve response'u döndür
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new JwtResponse(jwt,
                      userDetails.getId(),
                      userDetails.getUsername(),
                      userDetails.getEmail(),
                      roles));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        // JWT ve Refresh Cookie'leri temizle
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshCookie = jwtUtils.getCleanRefreshCookie();
        
        // Güvenlik bağlamını temizle
        SecurityContextHolder.clearContext();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new MessageResponse("Başarıyla çıkış yapıldı!"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(signUpRequest.getRole() != null ? signUpRequest.getRole() : Role.ROLE_USER);

        // Kasa hesabı otomatik oluşturmak için userService.saveUser kullanılıyor
        userService.saveUser(user);

        // Create trial subscription for new user
        subscriptionService.createSubscription(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    /**
     * Şifremi unuttum işlevi - Sadece admin rolündeki kullanıcılar tarafından kullanılabilir.
     * Bu endpoint, müşterinin kullanıcı adı ve e-posta bilgilerini alarak yeni bir rastgele şifre oluşturur.
     * 
     * @param passwordResetRequest Kullanıcı adı ve e-posta içeren istek nesnesi
     * @return Yeni şifre veya hata mesajı
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        String username = passwordResetRequest.getUsername();
        String email = passwordResetRequest.getEmail();
        
        // Kullanıcı adı ve e-postaya göre kullanıcıyı bul ve şifresini sıfırla
        String newPassword = userService.resetPassword(username, email);
        
        if (newPassword != null) {
            return ResponseEntity.ok(new PasswordResetResponse("Şifre başarıyla sıfırlandı. Yeni şifre: " + newPassword, true));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new PasswordResetResponse("Kullanıcı bulunamadı veya bilgiler eşleşmiyor.", false));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getRefreshTokenFromRequest(request);
        
        if (refreshToken != null && jwtUtils.validateJwtToken(refreshToken) && jwtUtils.isRefreshToken(refreshToken)) {
            try {
                // Refresh token'dan kullanıcı adını al
                String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
                
                // Kullanıcı bilgilerini yükle
                UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                
                // Abonelik kontrolü
                if (!subscriptionService.isSubscriptionValid(userDetails.getId())) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Aboneliğiniz bitti, lütfen servispilot@hotmail.com ile iletişime geçiniz."));
                }
                
                // Yeni access token oluştur
                String newAccessToken = jwtUtils.generateTokenFromUsername(username);
                
                // Yeni access token'ı cookie olarak oluştur - özel method oluşturalım
                ResponseCookie jwtCookie = createTokenCookie(newAccessToken);
                
                // Kullanıcı rolleri
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(item -> item.getAuthority())
                        .collect(Collectors.toList());
                
                // Cookie'yi header'a ekle ve response'u döndür
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                        .body(new JwtResponse(newAccessToken,
                                userDetails.getId(),
                                userDetails.getUsername(),
                                userDetails.getEmail(),
                                roles));
            } catch (Exception e) {
                logger.error("Refresh token işlemi sırasında hata: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Oturum yenileme başarısız oldu: " + e.getMessage()));
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Refresh token geçersiz veya süresi doldu. Lütfen tekrar giriş yapın."));
    }
    
    // Token'ı cookie olarak oluşturan yardımcı metod
    private ResponseCookie createTokenCookie(String token) {
        return ResponseCookie.from(JwtUtils.JWT_COOKIE_NAME, token)
                .path("/")
                .maxAge(jwtUtils.getJwtExpirationMs() / 1000)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();
    }
} 