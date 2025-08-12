package com.auth.service.security;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require specific permissions for method access
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * Required permissions. User must have at least one of these permissions.
     */
    String[] value();

    /**
     * If true, user must have ALL specified permissions.
     * If false (default), user needs at least ONE of the specified permissions.
     */
    boolean requireAll() default false;
}