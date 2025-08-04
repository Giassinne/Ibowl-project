package com.example.ibowl.service;

import com.example.ibowl.entity.User;
import com.example.ibowl.entity.Role;
import com.example.ibowl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        System.out.println("UserService: findByEmail called with email: " + email);
        Optional<User> user = userRepository.findByEmailIgnoreCase(email);
        if (user.isPresent()) {
            System.out.println("UserService: Found user: " + user.get().getEmail() + " - " + user.get().getFirstName() + " " + user.get().getLastName());
        } else {
            System.out.println("UserService: No user found with email: " + email);
        }
        return user;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    // Admin methods
    public long countAllUsers() {
        return userRepository.count();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUserFromAdmin(Map<String, Object> userData) {
        String email = (String) userData.get("email");
        String password = (String) userData.get("password");
        String firstName = (String) userData.get("firstName");
        String lastName = (String) userData.get("lastName");
        String roleStr = (String) userData.get("role");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (roleStr == null || roleStr.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone((String) userData.get("phone"));
        user.setRole(Role.valueOf(roleStr));
        user.setLoyaltyPoints(0);
        return userRepository.save(user);
    }

    public User updateUserFromAdmin(Long userId, Map<String, Object> userData) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEmail((String) userData.get("email"));
        user.setFirstName((String) userData.get("firstName"));
        user.setLastName((String) userData.get("lastName"));
        user.setPhone((String) userData.get("phone"));
        user.setRole(Role.valueOf((String) userData.get("role")));
        
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public User toggleUserStatus(Long userId, Boolean isActive) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // For now, we'll just return the user as is since we don't have an isActive field
        // In a real implementation, you'd update the user's active status
        return user;
    }

    public long countCustomers() {
        return userRepository.countByRole(Role.CUSTOMER);
    }

    public long countNewCustomers(LocalDateTime since) {
        return userRepository.countByRoleAndCreatedAtAfter(Role.CUSTOMER, since);
    }

    public List<Map<String, Object>> getTopCustomers() {
        // This would typically query for customers with most bookings or highest loyalty points
        // For now, return a simple list
        List<User> customers = userRepository.findByRoleOrderByLoyaltyPointsDesc(Role.CUSTOMER);
        return customers.stream()
            .limit(10)
            .map(user -> {
                Map<String, Object> customerMap = new HashMap<>();
                customerMap.put("id", user.getId());
                customerMap.put("name", user.getFirstName() + " " + user.getLastName());
                customerMap.put("email", user.getEmail());
                customerMap.put("loyaltyPoints", user.getLoyaltyPoints());
                return customerMap;
            })
            .toList();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
} 