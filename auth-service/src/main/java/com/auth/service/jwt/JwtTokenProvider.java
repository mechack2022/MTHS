package com.auth.service.jwt;//package com.auth.service.jwt;//package com.digi_dokita.auth.jwt;
////
//import com.auth.service.repository.UserRepository;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.stereotype.Component;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//
//import javax.crypto.SecretKey;
//import java.security.Key;
//import java.security.SecureRandom;
//import java.util.Base64;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//public class JwtTokenProvider {
//    private final String jwtSecret = generateSecretKey();
//    private final UserRepository userRepository;
//
//    public JwtTokenProvider(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    private Key key() {
//        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
//    }
//
//    public String generateSecretKey() {
//        int length = 32; // 32 bytes for 256-bit key
//        SecureRandom secureRandom = new SecureRandom();
//        byte[] keyBytes = new byte[length];
//        secureRandom.nextBytes(keyBytes);
//        return Base64.getEncoder().encodeToString(keyBytes);
//    }
//
//    public String getUsername(String token) {
//        return Jwts.parser()
//                .verifyWith((SecretKey) key())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject();
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parser()
//                    .verifyWith((SecretKey) key())
//                    .build()
//                    .parse(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public String generateToken(Authentication authentication) {
//        String username = authentication.getName();
//        Date currentDate = new Date();
//        // 1 hour in milliseconds
//        long jwtExpirationDate = 3600000;
//        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
//
//        // Retrieve user details from the database
//        var user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Extract roles from the Authentication object
//        List<String> roles = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//
//        return Jwts.builder()
//                .subject(username)
//                .issuedAt(currentDate)
//                .expiration(expireDate)
//                .claim("roles", roles)
//                .claim("current user", user.getAccountType())
//                .signWith(key(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public List<GrantedAuthority> getRolesFromToken(String token) {
//        Claims claims = Jwts.parser()
//                .verifyWith((SecretKey) key())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//
//        List<String> roles = claims.get("roles", List.class);
//        return roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//    }
//
//}


import com.auth.service.repository.UserRepository; // Update this import based on your actual package
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:myDefaultSecretKeyThatIsAtLeast32CharactersLong}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:3600000}")
    private long jwtExpirationDate;

    @Value("${app.jwt.refresh-expiration:6400000}")
    private long refreshTokenExpirationDate;

    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        // Retrieve user details from the database
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Extract roles and permissions from user
        List<String> roles = user.getRoles().stream()
                .map(userRole -> userRole.getRoleName().name())
                .collect(Collectors.toList());

        // Extract permissions from roles
        List<String> permissions = user.getRoles().stream()
                .flatMap(userRole -> userRole.getPermissions().stream())
                .map(permission -> permission.getPermissionName().name())
                .distinct()
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("accountType", user.getAccountType())
                .claim("userId", user.getId())
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Overloaded method for generating token with username only
    public String generateToken(String username) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        // Retrieve user details from the database
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Extract roles and permissions from user
        List<String> roles = user.getRoles().stream()
                .map(userRole -> userRole.getRoleName().name())
                .collect(Collectors.toList());

        // Extract permissions from roles
        List<String> permissions = user.getRoles().stream()
                .flatMap(userRole -> userRole.getPermissions().stream())
                .map(permission -> permission.getPermissionName().name())
                .distinct()
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("accountType", user.getAccountType())
                .claim("userId", user.getId())
                .claim("type", "access")
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public long getTokenExpiration() {
        return jwtExpirationDate / 1000;
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken) || !isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = getUsername(refreshToken);
        return generateToken(username);
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpirationDate / 1000;
    }
    public String generateRefreshToken(String username) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + refreshTokenExpirationDate);

        // Retrieve user details from the database
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Jwts.builder()
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public List<GrantedAuthority> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roles = claims.get("roles", List.class);
        List<String> permissions = claims.get("permissions", List.class);

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add roles with ROLE_ prefix
        if (roles != null) {
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList()));
        }

        // Add permissions as authorities
        if (permissions != null) {
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        return authorities;
    }

    public List<String> getRolesOnlyFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("roles", List.class);
    }

    public List<String> getPermissionsFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("permissions", List.class);
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", String.class);
    }

    public String getAccountTypeFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("accountType", String.class);
    }
}