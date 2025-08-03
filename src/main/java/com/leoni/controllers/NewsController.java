package com.leoni.controllers;

import com.leoni.models.Admin;
import com.leoni.models.News;
import com.leoni.services.AdminService;
import com.leoni.services.NewsService;
import com.leoni.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);
    
    @Autowired
    private NewsService newsService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private AdminService adminService;
    
    /**
     * Helper method to extract token from Authorization header
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }
        return authHeader;
    }
    
    /**
     * Create a new news article
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createNews(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        
        try {
            // Extract and validate token
            String token = extractToken(authToken);
            if (token == null || !authService.validateToken(token)) {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Token d'authentification requis")
                );
            }
            
            String role = authService.getRoleFromToken(token);
            String userId = authService.getUserIdFromToken(token);
            
            if (role == null || userId == null) {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Token invalide")
                );
            }
            
            // Create news object
            News news = new News();
            news.setTitle((String) request.get("title"));
            news.setContent((String) request.get("content"));
            news.setSummary((String) request.get("summary"));
            news.setCategory((String) request.get("category"));
            news.setPriority((String) request.get("priority"));
            
            // Handle targeting based on role
            if ("SUPERADMIN".equals(role)) {
                // SuperAdmins can set custom targeting
                news.setTargetLocation((String) request.get("targetLocation"));
                news.setTargetDepartment((String) request.get("targetDepartment"));
            } else if ("ADMIN".equals(role)) {
                // For regular admins, automatically set targeting based on their profile
                try {
                    Admin admin = adminService.getAdminById(userId);
                    if (admin != null) {
                        if (admin.getLocation() != null && !admin.getLocation().trim().isEmpty()) {
                            news.setTargetLocation(admin.getLocation());
                        }
                        if (admin.getDepartment() != null && !admin.getDepartment().trim().isEmpty()) {
                            news.setTargetDepartment(admin.getDepartment());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting admin profile for auto-targeting: " + e.getMessage());
                }
            }
            
            // Set visibility status
            String visibilityStatus = "draft"; // default value
            Object visibilityObj = request.get("visibility");
            if (visibilityObj instanceof Map) {
                Map<String, Object> visibilityMap = (Map<String, Object>) visibilityObj;
                visibilityStatus = (String) visibilityMap.getOrDefault("status", "draft");
            } else if (request.containsKey("status")) {
                // Fallback for direct status field
                visibilityStatus = (String) request.getOrDefault("status", "draft");
            }
            
            // Use legacy format (isActive) instead of visibility.status
            news.setIsActive("published".equals(visibilityStatus) || "draft".equals(visibilityStatus));
            
            // Create the news
            News createdNews = newsService.createNews(news, userId, role);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Actualité créée avec succès",
                "news", createdNews
            ));
            
        } catch (Exception e) {
            logger.error("Error creating news", e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Erreur lors de la création de l'actualité: " + e.getMessage())
            );
        }
    }
    
    /**
     * Alternative POST endpoint on /api/news (for compatibility with frontend)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNewsAlternative(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        // Redirect to the main create method
        return createNews(request, authToken);
    }
    
    /**
     * Get news based on user role and filters
     */
    @GetMapping
    public ResponseEntity<List<News>> getNews(
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        
        try {
            // Extract token from Authorization header
            String token = authToken;
            if (authToken != null && authToken.startsWith("Bearer ")) {
                token = authToken.replace("Bearer ", "");
            }
            
            // Validate token
            if (token == null || !authService.validateToken(token)) {
                logger.warn("Invalid or missing token: {}", token);
                return ResponseEntity.status(401).build();
            }
            
            String role = authService.getRoleFromToken(token);
            String userId = authService.getUserIdFromToken(token);
            
            List<News> news;
            
            if ("SUPERADMIN".equals(role)) {
                // SuperAdmin sees all news with optional filtering
                if (location != null && department != null) {
                    news = newsService.getFilteredNews(location, department, category, priority);
                } else {
                    // For SuperAdmin, show ALL news regardless of status
                    news = newsService.getAllNews();
                    
                    // Apply filters on all news
                    if (category != null && !category.isEmpty()) {
                        news = news.stream().filter(n -> category.equals(n.getCategory())).toList();
                    }
                    if (priority != null && !priority.isEmpty()) {
                        news = news.stream().filter(n -> priority.equals(n.getPriority())).toList();
                    }
                }
            } else {
                // Admin sees only their targeted news
                news = newsService.getNewsForAdmin(userId);
                
                // Apply additional filters if provided
                if (category != null) {
                    news = news.stream().filter(n -> category.equals(n.getCategory())).toList();
                }
                if (priority != null) {
                    news = news.stream().filter(n -> priority.equals(n.getPriority())).toList();
                }
            }
            
            return ResponseEntity.ok(news);
            
        } catch (Exception e) {
            logger.error("Error fetching news", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * DEBUG: Get ALL news regardless of status - for troubleshooting
     */
    @GetMapping("/debug/all")
    public ResponseEntity<List<News>> getAllNewsDebug() {
        try {
            List<News> news = newsService.getAllNews();
            logger.info("Found {} news items in database", news.size());
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            logger.error("Error fetching all news for debug", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get news for employees (public endpoint with location/department filtering)
     */
    @GetMapping("/public")
    public ResponseEntity<List<News>> getPublicNews(
            @RequestParam("location") String location,
            @RequestParam("department") String department,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priority", required = false) String priority) {
        
        try {
            List<News> news = newsService.getFilteredNews(location, department, category, priority);
            return ResponseEntity.ok(news);
            
        } catch (Exception e) {
            logger.error("Error fetching public news", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get news by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable String id) {
        try {
            Optional<News> news = newsService.getNewsById(id);
            if (news.isPresent()) {
                return ResponseEntity.ok(news.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching news by ID", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Update news
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateNews(
            @PathVariable String id,
            @RequestBody News updatedNews,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        
        try {
            // Extract and validate token
            String token = extractToken(authToken);
            if (token == null || !authService.validateToken(token)) {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Token d'authentification requis")
                );
            }
            
            News news = newsService.updateNews(id, updatedNews);
            if (news != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Actualité mise à jour avec succès",
                    "news", news
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error updating news", e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Erreur lors de la mise à jour: " + e.getMessage())
            );
        }
    }
    
    /**
     * Publish news
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishNews(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        
        try {
            // Extract and validate token
            String token = extractToken(authToken);
            if (token == null || !authService.validateToken(token)) {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Token d'authentification requis")
                );
            }
            
            News news = newsService.publishNews(id);
            if (news != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Actualité publiée avec succès",
                    "news", news
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error publishing news", e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Erreur lors de la publication: " + e.getMessage())
            );
        }
    }
    
    /**
     * Archive news
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<Map<String, Object>> archiveNews(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        
        try {
            // Extract and validate token
            String token = extractToken(authToken);
            if (token == null || !authService.validateToken(token)) {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Token d'authentification requis")
                );
            }
            
            News news = newsService.archiveNews(id);
            if (news != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Actualité archivée avec succès",
                    "news", news
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error archiving news", e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Erreur lors de l'archivage: " + e.getMessage())
            );
        }
    }
    
    /**
     * Delete news
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNews(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        
        try {
            // Extract and validate token
            String token = extractToken(authToken);
            if (token == null || !authService.validateToken(token)) {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Token d'authentification requis")
                );
            }
            
            boolean deleted = newsService.deleteNews(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Actualité supprimée avec succès"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error deleting news", e);
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Erreur lors de la suppression: " + e.getMessage())
            );
        }
    }
}
