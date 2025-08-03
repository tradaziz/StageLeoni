package com.leoni.repositories;

import com.leoni.models.SuperAdmin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuperAdminRepository extends MongoRepository<SuperAdmin, String> {
    
    Optional<SuperAdmin> findByUsername(String username);
    
    Optional<SuperAdmin> findByUsernameAndActiveTrue(String username);
    
    List<SuperAdmin> findByActiveTrue();
    
    boolean existsByUsername(String username);
    
    @Query("{'username': ?0, 'active': true}")
    Optional<SuperAdmin> findActiveSuperAdminByUsername(String username);
}
