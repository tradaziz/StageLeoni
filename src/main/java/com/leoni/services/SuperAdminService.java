package com.leoni.services;

import com.leoni.models.SuperAdmin;
import com.leoni.repositories.SuperAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminService {
    
    @Autowired
    private SuperAdminRepository superAdminRepository;
    
    /**
     * Create a new superadmin
     */
    public SuperAdmin createSuperAdmin(SuperAdmin superAdmin) {
        if (superAdminRepository.existsByUsername(superAdmin.getUsername())) {
            throw new RuntimeException("SuperAdmin with username '" + superAdmin.getUsername() + "' already exists");
        }
        
        superAdmin.setCreatedAt(LocalDateTime.now());
        superAdmin.setUpdatedAt(LocalDateTime.now());
        
        return superAdminRepository.save(superAdmin);
    }
    
    /**
     * Authenticate superadmin
     */
    public Optional<SuperAdmin> authenticate(String username, String password) {
        Optional<SuperAdmin> superAdmin = superAdminRepository.findByUsernameAndActiveTrue(username);
        
        if (superAdmin.isPresent() && superAdmin.get().getPassword().equals(password)) {
            return superAdmin;
        }
        
        return Optional.empty();
    }
    
    /**
     * Get all active superadmins
     */
    public List<SuperAdmin> getAllActiveSuperAdmins() {
        return superAdminRepository.findByActiveTrue();
    }
    
    /**
     * Find superadmin by ID
     */
    public Optional<SuperAdmin> findById(String id) {
        return superAdminRepository.findById(id);
    }
    
    /**
     * Find superadmin by username
     */
    public Optional<SuperAdmin> findByUsername(String username) {
        return superAdminRepository.findByUsernameAndActiveTrue(username);
    }
    
    /**
     * Update superadmin
     */
    public SuperAdmin updateSuperAdmin(SuperAdmin superAdmin) {
        superAdmin.setUpdatedAt(LocalDateTime.now());
        return superAdminRepository.save(superAdmin);
    }
    
    /**
     * Delete superadmin (soft delete)
     */
    public void deleteSuperAdmin(String id) {
        Optional<SuperAdmin> superAdmin = superAdminRepository.findById(id);
        if (superAdmin.isPresent()) {
            SuperAdmin superAdminToDelete = superAdmin.get();
            superAdminToDelete.setActive(false);
            superAdminToDelete.setUpdatedAt(LocalDateTime.now());
            superAdminRepository.save(superAdminToDelete);
        }
    }
    
    /**
     * Save superadmin
     */
    public SuperAdmin save(SuperAdmin superAdmin) {
        return superAdminRepository.save(superAdmin);
    }
}
