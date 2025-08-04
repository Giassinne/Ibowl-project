package com.example.ibowl.repository;

import com.example.ibowl.entity.User;
import com.example.ibowl.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);
    
    // Admin methods
    long countByRole(Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt >= :since")
    long countByRoleAndCreatedAtAfter(@Param("role") Role role, @Param("since") LocalDateTime since);
    
    List<User> findByRoleOrderByLoyaltyPointsDesc(Role role);
} 