package com.mths.auth.controller;

import com.mths.shared.dto.ApiResponse;
import com.mths.auth.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import com.mths.auth.dto.UserDTO;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserAuthService userAuthService;

    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getPendingUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Admin fetching pending users - Page: {}, Size: {}", page, size);
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserDTO> pendingUsers = userAuthService.getPendingUsers(pageable);
        
        ApiResponse<Page<UserDTO>> response = ApiResponse.success(
            "Pending users retrieved successfully",
            pendingUsers,
            HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/verify")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> verifyUserAccount(@PathVariable String userId) {
        log.info("Admin verifying user account with ID: {}", userId);
        
        UserDTO verifiedUser = userAuthService.verifyUserAccount(userId);
        
        ApiResponse<UserDTO> response = ApiResponse.success(
            "User account verified successfully",
            verifiedUser,
            HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> rejectUserAccount(
            @PathVariable String userId,
            @RequestBody @Valid RejectUserRequest request) {
        
        log.info("Admin rejecting user account with ID: {} with reason: {}", userId, request.reason());
        
        userAuthService.rejectUserAccount(userId, request.reason());
        
        ApiResponse<String> response = ApiResponse.success(
            "User account rejected successfully",
            "Account rejected",
            HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserDetails(@PathVariable String userId) {
        log.info("Admin fetching user details for ID: {}", userId);
        
        UserDTO user = userAuthService.getUserById(userId);
        
        ApiResponse<UserDTO> response = ApiResponse.success(
            "User details retrieved successfully",
            user,
            HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> activateUser(@PathVariable String userId) {
        log.info("Admin activating user account with ID: {}", userId);
        
        userAuthService.reactivateAccount(userId);
        UserDTO user = userAuthService.getUserById(userId);
        
        ApiResponse<UserDTO> response = ApiResponse.success(
            "User account activated successfully",
            user,
            HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> deactivateUser(@PathVariable String userId) {
        log.info("Admin deactivating user account with ID: {}", userId);
        
        userAuthService.deactivateAccount(userId);
        UserDTO user = userAuthService.getUserById(userId);
        
        ApiResponse<UserDTO> response = ApiResponse.success(
            "User account deactivated successfully",
            user,
            HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    public record RejectUserRequest(String reason) {}
}