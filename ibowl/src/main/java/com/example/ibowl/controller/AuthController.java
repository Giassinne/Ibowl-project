package com.example.ibowl.controller;

import com.example.ibowl.dto.request.LoginRequest;
import com.example.ibowl.dto.request.RegisterRequest;
import com.example.ibowl.dto.request.UpdateUserRequest;
import com.example.ibowl.dto.response.JwtResponse;
import com.example.ibowl.dto.response.UserResponse;
import com.example.ibowl.entity.User;
import com.example.ibowl.entity.Role;
import com.example.ibowl.security.JwtTokenProvider;
import com.example.ibowl.security.UserPrincipal;
import com.example.ibowl.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Login endpoint for email/password authentication
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            logger.info("Login attempt for email: {}", request.getEmail());
            
            // Check if user exists first
            if (!userService.existsByEmail(request.getEmail())) {
                logger.warn("Login failed: User not found - {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid email or password"));
            }
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();
            
            // Generate JWT token
            String token = jwtTokenProvider.generateToken(userPrincipal.getUsername());
            
            // Create response
            JwtResponse response = new JwtResponse();
            response.setToken(token);
            response.setType("Bearer");
            response.setRole(user.getRole().name());
            response.setEmail(user.getEmail());
            
            logger.info("Login successful for user: {} with role: {}", request.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
                
        } catch (Exception e) {
            logger.error("Login error for email {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Invalid email or password"));
        }
    }

    /**
     * Register new user (customer role by default)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.info("Registration attempt for email: {}", request.getEmail());
            
            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                logger.warn("Registration failed: Email already exists - {}", request.getEmail());
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Email already exists: " + request.getEmail()));
            }
            
            // Create new user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(request.getPhone());
            user.setRole(Role.CUSTOMER); // Default role
            user.setLoyaltyPoints(0);
            
            User savedUser = userService.save(user);
            
            UserResponse response = convertToUserResponse(savedUser);
            
            logger.info("User registered successfully: {}", savedUser.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Registration error for email {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Create admin user (restricted endpoint)
     */
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.info("Admin creation attempt for email: {}", request.getEmail());
            
            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                logger.warn("Admin creation failed: Email already exists - {}", request.getEmail());
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Email already exists: " + request.getEmail()));
            }
            
            // Create admin user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(request.getPhone());
            user.setRole(Role.ADMIN);
            user.setLoyaltyPoints(0);
            
            User savedUser = userService.save(user);
            
            UserResponse response = convertToUserResponse(savedUser);
            
            logger.info("Admin user created successfully: {}", savedUser.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Admin creation error for email {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Admin creation failed: " + e.getMessage()));
        }
    }

    /**
     * Google OAuth2 login
     */
    @PostMapping("/oauth2/google")
    public ResponseEntity<?> googleOAuth2Login(@RequestBody Map<String, String> payload) {
        String idTokenString = payload.get("idToken");
        
        if (idTokenString == null || idTokenString.trim().isEmpty()) {
            logger.warn("Google OAuth2 login failed: Missing idToken");
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Missing idToken"));
        }
        
        try {
            logger.info("Google OAuth2 login attempt");
            
            // Verify Google ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList("114463283400-ab1ldu42nao93fauu9po8rcg9on8cln7.apps.googleusercontent.com"))
                    .build();
                    
            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken != null) {
                GoogleIdToken.Payload googlePayload = idToken.getPayload();
                String email = googlePayload.getEmail();
                String firstName = (String) googlePayload.get("given_name");
                String lastName = (String) googlePayload.get("family_name");
                
                logger.info("Google OAuth2 verified for email: {}", email);
                
                // Check if user exists
                Optional<User> userOpt = userService.findByEmail(email);
                User user;
                
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                    // Always force role to CUSTOMER for OAuth users
                    if (user.getRole() != Role.CUSTOMER) {
                        user.setRole(Role.CUSTOMER);
                        user = userService.save(user);
                        logger.info("Updated existing user role to CUSTOMER: {}", email);
                    }
                } else {
                    // Create new user
                    user = new User();
                    user.setEmail(email);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setRole(Role.CUSTOMER);
                    user.setLoyaltyPoints(0);
                    user = userService.save(user);
                    logger.info("Created new OAuth user: {}", email);
                }
                
                // Generate JWT for your app
                String jwt = jwtTokenProvider.generateToken(user.getEmail());
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", jwt);
                response.put("type", "Bearer");
                response.put("email", user.getEmail());
                response.put("role", user.getRole().name());
                
                return ResponseEntity.ok(response);
                
            } else {
                logger.warn("Google OAuth2 failed: Invalid ID token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid ID token"));
            }
            
        } catch (Exception e) {
            logger.error("Google OAuth2 error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Google token verification failed"));
        }
    }

    /**
     * Get current user information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal, 
                                          HttpServletRequest request) {
        try {
            // Debug logging
            String authHeader = request.getHeader("Authorization");
            logger.debug("Auth header present: {}", authHeader != null);
            logger.debug("UserPrincipal: {}", userPrincipal);
            
            if (userPrincipal == null) {
                logger.warn("Unauthorized access to /me endpoint - UserPrincipal is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }
            
            User user = userPrincipal.getUser();
            if (user == null) {
                logger.warn("Unauthorized access to /me endpoint - User is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not found"));
            }
            
            logger.debug("Current user request for: {}", user.getEmail());
            
            UserResponse response = convertToUserResponse(user);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get user information"));
        }
    }

    /**
     * Update current user information
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal, 
                                             @Valid @RequestBody UpdateUserRequest request) {
        try {
            if (userPrincipal == null) {
                logger.warn("Unauthorized access to update /me endpoint");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }
            
            User user = userPrincipal.getUser();
            if (user == null) {
                logger.warn("User not found for update");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not found"));
            }
            
            logger.info("Updating user: {}", user.getEmail());
            
            // Update user fields
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // Check if new email already exists (for different user)
                if (!request.getEmail().equals(user.getEmail()) && userService.existsByEmail(request.getEmail())) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email already exists"));
                }
                user.setEmail(request.getEmail().trim());
            }
            
            if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                user.setFirstName(request.getFirstName().trim());
            }
            
            if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
                user.setLastName(request.getLastName().trim());
            }
            
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                user.setPhone(request.getPhone().trim());
            }
            
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            
            User savedUser = userService.save(user);
            UserResponse response = convertToUserResponse(savedUser);
            
            logger.info("User updated successfully: {}", savedUser.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update user"));
        }
    }

    /**
     * Test endpoint to verify controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Auth controller is working!");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint (invalidate token on client side)
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Since we're using stateless JWT, logout is handled client-side
        // You could implement token blacklisting here if needed
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    // Helper methods

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setLoyaltyPoints(user.getLoyaltyPoints());
        return response;
    }

    /**
     * Create standardized error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        return error;
    }
}