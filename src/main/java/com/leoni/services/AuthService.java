package com.leoni.services;

import com.leoni.dto.AuthRequest;
import com.leoni.dto.AuthResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    
    // Simple hardcoded admin credentials as specified
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    
    /**
     * Authenticate admin user
     * @param authRequest the authentication request
     * @return AuthResponse with authentication result
     */
    public AuthResponse authenticate(AuthRequest authRequest) {
        if (authRequest == null || authRequest.getUsername() == null || authRequest.getPassword() == null) {
            return AuthResponse.failure("Nom d'utilisateur et mot de passe requis");
        }
        
        // Check credentials
        if (ADMIN_USERNAME.equals(authRequest.getUsername()) && 
            ADMIN_PASSWORD.equals(authRequest.getPassword())) {
            
            // Generate a simple token (in production, use JWT or similar)
            String token = generateToken();
            return AuthResponse.success(token, ADMIN_USERNAME);
        } else {
            return AuthResponse.invalidCredentials();
        }
    }
    
    /**
     * Validate authentication token
     * @param token the token to validate
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        // Simple token validation (in production, implement proper JWT validation)
        return token != null && token.startsWith("admin-token-");
    }
    
    /**
     * Extract username from token
     * @param token the authentication token
     * @return username if token is valid, null otherwise
     */
    public String getUsernameFromToken(String token) {
        if (validateToken(token)) {
            return ADMIN_USERNAME;
        }
        return null;
    }
    
    /**
     * Logout user (invalidate token)
     * @param token the token to invalidate
     * @return true if logout successful
     */
    public boolean logout(String token) {
        // In a real implementation, you would add the token to a blacklist
        // For this simple implementation, we just return true
        return validateToken(token);
    }
    
    /**
     * Check if user is admin
     * @param username the username to check
     * @return true if user is admin
     */
    public boolean isAdmin(String username) {
        return ADMIN_USERNAME.equals(username);
    }
    
    /**
     * Generate a simple authentication token
     * @return generated token
     */
    private String generateToken() {
        return "admin-token-" + UUID.randomUUID().toString();
    }
    
    /**
     * Get admin username (for configuration purposes)
     * @return admin username
     */
    public String getAdminUsername() {
        return ADMIN_USERNAME;
    }
}