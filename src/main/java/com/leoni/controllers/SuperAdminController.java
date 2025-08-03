package com.leoni.controllers;

import com.leoni.models.SuperAdmin;
import com.leoni.services.SuperAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/superadmin")
@CrossOrigin(origins = "*")
public class SuperAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(SuperAdminController.class);
    
    @Autowired
    private SuperAdminService superAdminService;
    
    /**
     * Create a new superadmin
     */
    @PostMapping("/create")
    public ResponseEntity<?> createSuperAdmin(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Nom d'utilisateur et mot de passe requis")
                );
            }
            
            SuperAdmin superAdmin = new SuperAdmin(username, password);
            SuperAdmin createdSuperAdmin = superAdminService.createSuperAdmin(superAdmin);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "SuperAdmin créé avec succès",
                "superadmin", Map.of(
                    "id", createdSuperAdmin.getId(),
                    "username", createdSuperAdmin.getUsername(),
                    "role", createdSuperAdmin.getRole(),
                    "active", createdSuperAdmin.isActive(),
                    "createdAt", createdSuperAdmin.getCreatedAt()
                )
            ));
            
        } catch (Exception e) {
            logger.error("Error creating superadmin", e);
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }
    
    /**
     * Authenticate superadmin
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateSuperAdmin(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            Optional<SuperAdmin> superAdmin = superAdminService.authenticate(username, password);
            
            if (superAdmin.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Authentification réussie",
                    "superadmin", Map.of(
                        "id", superAdmin.get().getId(),
                        "username", superAdmin.get().getUsername(),
                        "role", superAdmin.get().getRole()
                    )
                ));
            } else {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Nom d'utilisateur ou mot de passe incorrect")
                );
            }
            
        } catch (Exception e) {
            logger.error("Error authenticating superadmin", e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Erreur lors de l'authentification")
            );
        }
    }
    
    /**
     * Get all active superadmins
     */
    @GetMapping("/all")
    public ResponseEntity<List<SuperAdmin>> getAllSuperAdmins() {
        try {
            List<SuperAdmin> superAdmins = superAdminService.getAllActiveSuperAdmins();
            return ResponseEntity.ok(superAdmins);
        } catch (Exception e) {
            logger.error("Error fetching superadmins", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get superadmin by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSuperAdminById(@PathVariable String id) {
        try {
            Optional<SuperAdmin> superAdmin = superAdminService.findById(id);
            if (superAdmin.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "superadmin", superAdmin.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching superadmin by ID", e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Erreur lors de la récupération du SuperAdmin")
            );
        }
    }
    
    /**
     * Delete superadmin (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSuperAdmin(@PathVariable String id) {
        try {
            superAdminService.deleteSuperAdmin(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "SuperAdmin supprimé avec succès"
            ));
        } catch (Exception e) {
            logger.error("Error deleting superadmin", e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Erreur lors de la suppression du SuperAdmin")
            );
        }
    }
}
