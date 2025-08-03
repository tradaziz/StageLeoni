package com.leoni.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/simple-auth")
@CrossOrigin(origins = "*")
public class SimpleAuthController {
    
    /**
     * Simple authentication endpoint without Lombok dependencies
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> simpleLogin(@RequestBody Map<String, String> credentials) {
        System.out.println("=== SIMPLE AUTH ENDPOINT CALLED ===");
        System.out.println("Received credentials: " + credentials);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            
            // Check credentials
            if ("admin".equals(username) && "admin".equals(password)) {
                String token = "admin-token-" + UUID.randomUUID().toString();
                
                response.put("success", true);
                response.put("message", "Authentication successful");
                response.put("token", token);
                response.put("username", username);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
                
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            System.out.println("Exception in simple login: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Health check for simple auth
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Simple auth service is operational");
        return ResponseEntity.ok(response);
    }
}