package com.auth.service.jwt;//package com.digi_dokita.auth.jwt;
////
////
////import com.digi_dokita.entity.User;
////import com.digi_dokita.exceptions.BadRequestException;
////import com.digi_dokita.exceptions.ResourceNotFoundException;
////import com.digi_dokita.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
//
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Component
//public class CustomUserDetailsService implements UserDetailsService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        Set<GrantedAuthority> authorities = user.getRoles().stream()
//                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleName().name()))
//                .collect(Collectors.toSet());
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getEmail(),
//                user.getPassword(),
//                user.getMailVerified(),         // enabled
//                true,                    // accountNonExpired
//                true,                    // credentialsNonExpired
//                true,                    // accountNonLocked
//                authorities
//        );
//    }
//
//
//    public User getUserByEmail(String email) {
//        if (email == null) {
//            throw new BadRequestException("User", "Email is required");
//        }
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "Email Not Found", email));
//    }
//
//    public User getUserByUsername(String username) {
//        if (username == null) {
//            throw new BadRequestException("User", "Email is required");
//        }
//        return userRepository.findByEmail(username)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "username Not Found", username
//                ));
//    }
//
//}


import com.auth.service.entity.User;
import com.auth.service.repository.UserRepository;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add roles with ROLE_ prefix
        user.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName().name()))
        );

        // Add permissions from all roles
        user.getRoles().forEach(role ->
                role.getPermissions().forEach(permission ->
                        authorities.add(new SimpleGrantedAuthority(permission.getPermissionName().name()))
                )
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getMailVerified() != null ? user.getMailVerified() : false,         // enabled
                true,                    // accountNonExpired
                true,                    // credentialsNonExpired
                user.getIsActive() != null ? user.getIsActive() : true,                    // accountNonLocked
                authorities
        );
    }

    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("User", "Email is required");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email Not Found", email));
    }

    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("User", "Username is required");
        }
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Username Not Found", username));
    }
}