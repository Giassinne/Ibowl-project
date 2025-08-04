package com.example.ibowl;

import com.example.ibowl.entity.User;
import com.example.ibowl.entity.Role;
import com.example.ibowl.repository.UserRepository;
import com.example.ibowl.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    private final String testEmail = "testuser@ibowl.com";

    @AfterEach
    void cleanup() {
        userRepository.findByEmail(testEmail).ifPresent(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    void testCreateUserFromAdmin() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", testEmail);
        userData.put("password", "testpassword");
        userData.put("firstName", "Test");
        userData.put("lastName", "User");
        userData.put("phone", "+1-555-9999");
        userData.put("role", "CUSTOMER");

        User created = userService.createUserFromAdmin(userData);
        assertNotNull(created.getId(), "Created user should have an ID");
        assertEquals(testEmail, created.getEmail());
        assertEquals("Test", created.getFirstName());
        assertEquals("User", created.getLastName());
        assertEquals(Role.CUSTOMER, created.getRole());

        Optional<User> found = userRepository.findByEmail(testEmail);
        assertTrue(found.isPresent(), "User should be found by email after creation");
    }
} 