package com.auth.service.jwt;

import com.auth.service.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class for JWT operations in controllers
 */
@Component
public class JwtUtils {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Extract JWT token from request
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Get current authenticated user's username
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Get current user's roles from token
     */
    public List<String> getCurrentUserRoles(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getRolesOnlyFromToken(token);
        }
        return List.of();
    }

    /**
     * Get current user's permissions from token
     */
    public List<String> getCurrentUserPermissions(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getPermissionsFromToken(token);
        }
        return List.of();
    }

    /**
     * Get current user's ID from token
     */
    public String getCurrentUserId(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * Get current user's account type from token
     */
    public String getCurrentUserAccountType(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getAccountTypeFromToken(token);
        }
        return null;
    }

    /**
     * Check if current user has specific permission
     */
    public boolean hasPermission(HttpServletRequest request, String permission) {
        List<String> permissions = getCurrentUserPermissions(request);
        return permissions.contains(permission);
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(HttpServletRequest request, String role) {
        List<String> roles = getCurrentUserRoles(request);
        return roles.contains(role);
    }

    /**
     * Check if current user has any of the specified permissions
     */
    public boolean hasAnyPermission(HttpServletRequest request, String... permissions) {
        List<String> userPermissions = getCurrentUserPermissions(request);
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all of the specified permissions
     */
    public boolean hasAllPermissions(HttpServletRequest request, String... permissions) {
        List<String> userPermissions = getCurrentUserPermissions(request);
        for (String permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
}