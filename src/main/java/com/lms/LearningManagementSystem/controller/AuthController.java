package com.lms.LearningManagementSystem.controller;

import com.lms.LearningManagementSystem.model.User;
import com.lms.LearningManagementSystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // Remove password logging
        //System.out.println("Register endpoint accessed: " + user.getUsername());
        try {
            // Create a copy of the user for the response to avoid modifying the original
            User registeredUser = userService.createUser(user);

            // Create a clean response object instead of modifying the original user
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", registeredUser.getId());
            userResponse.put("username", registeredUser.getUsername());
            userResponse.put("firstName", registeredUser.getFirstName());
            userResponse.put("lastName", registeredUser.getLastName());
            userResponse.put("email", registeredUser.getEmail());
            userResponse.put("role", registeredUser.getRole());

            //registeredUser.setPassword(null);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Set session timeout (30 minutes)
            session.setMaxInactiveInterval(1800);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User logged in successfully");
            response.put("username", loginRequest.getUsername());
            response.put("role", authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));


            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            // Don't expose detailed error messages in production
            Map<String, String> response = new HashMap<>();
            response.put("error", "Authentication failed");
            // Log the actual error for debugging but don't return it to client
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
    // Add a method to check if a user is authenticated
    @GetMapping("/authenticated")
    public ResponseEntity<?> isAuthenticated(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("username", authentication.getName());
            response.put("role", authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
    }

    @Setter
    @Getter
    public static class LoginRequest {
        private String username;
        private String password;

    }
}