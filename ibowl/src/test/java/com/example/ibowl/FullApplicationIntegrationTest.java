package com.example.ibowl;

import com.example.ibowl.dto.request.RegisterRequest;
import com.example.ibowl.entity.User;
import com.example.ibowl.repository.BookingRepository;
import com.example.ibowl.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FullApplicationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private final String testEmail = "fullintegration@ibowl.com";
    private final String testPassword = "integrationpass";

    @AfterEach
    void cleanup() {
        userRepository.findByEmail(testEmail).ifPresent(user -> userRepository.deleteById(user.getId()));
        bookingRepository.findAll().stream()
            .filter(b -> b.getUser() != null && testEmail.equals(b.getUser().getEmail()))
            .forEach(b -> bookingRepository.deleteById(b.getId()));
    }

    @Test
    void testFullApplicationFlow() throws Exception {
        // 1. Register user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(testEmail);
        registerRequest.setPassword(testPassword);
        registerRequest.setFirstName("Full");
        registerRequest.setLastName("Integration");
        registerRequest.setPhone("+1-555-7777");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail));

        // 2. Login and get JWT
        String loginJson = "{\"email\":\"" + testEmail + "\", \"password\":\"" + testPassword + "\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode loginNode = objectMapper.readTree(responseBody);
        String jwt = loginNode.get("token").asText();
        assertNotNull(jwt);

        // 3. Create a booking
        Optional<User> userOpt = userRepository.findByEmail(testEmail);
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        // Find a laneId (use 1 if exists)
        Long laneId = 1L;
        String bookingJson = "{" +
                "\"userId\":" + user.getId() + "," +
                "\"laneId\":" + laneId + "," +
                "\"startTime\":\"2025-01-01T10:00:00\"," +
                "\"endTime\":\"2025-01-01T11:00:00\"," +
                "\"players\":2," +
                "\"totalPrice\":25.0" +
                "}";
        mockMvc.perform(post("/api/bookings")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()));

        // 4. Get bookings
        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(user.getId()));

        // 5. Try to access admin endpoint (should be forbidden or unauthorized)
        mockMvc.perform(get("/api/admin/dashboard/stats")
                .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }
} 