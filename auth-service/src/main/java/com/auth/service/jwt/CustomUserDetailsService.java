package com.auth.service.jwt;//package com.digi_dokita.auth.jwt;
//
//
//import com.digi_dokita.entity.User;
//import com.digi_dokita.exceptions.BadRequestException;
//import com.digi_dokita.exceptions.ResourceNotFoundException;
//import com.digi_dokita.repository.UserRepository;
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
