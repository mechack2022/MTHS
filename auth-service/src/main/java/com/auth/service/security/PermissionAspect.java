package com.auth.service.security;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userPermissions = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_")) // Filter out roles, keep only permissions
                .collect(Collectors.toSet());

        String[] requiredPermissions = requirePermission.value();
        boolean requireAll = requirePermission.requireAll();

        boolean hasPermission;
        if (requireAll) {
            // User must have ALL required permissions
            hasPermission = userPermissions.containsAll(Arrays.asList(requiredPermissions));
        } else {
            // User must have at least ONE required permission
            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            throw new AccessDeniedException("Insufficient permissions. Required: " +
                    Arrays.toString(requiredPermissions) + ", User has: " + userPermissions);
        }
        return joinPoint.proceed();
    }

    @Around("@within(requirePermission)")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        return checkPermission(joinPoint, requirePermission);
    }
}