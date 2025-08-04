package com.example.ibowl.controller;

import com.example.ibowl.entity.*;
import com.example.ibowl.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private LaneService laneService;

    // Dashboard Statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get total users
        long totalUsers = userService.countAllUsers();
        stats.put("totalUsers", totalUsers);
        
        // Get total bookings
        long totalBookings = bookingService.countAllBookings();
        stats.put("totalBookings", totalBookings);
        
        // Get total revenue
        BigDecimal totalRevenue = bookingService.getTotalRevenue();
        stats.put("totalRevenue", totalRevenue);
        
        // Get active bookings (confirmed)
        long activeBookings = bookingService.countActiveBookings();
        stats.put("activeBookings", activeBookings);
        
        return ResponseEntity.ok(stats);
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(@RequestParam(required = false) String email) {
        System.out.println("AdminController: getAllUsers called with email: " + email);
        
        List<User> users;
        if (email != null && !email.trim().isEmpty()) {
            // Search by email
            System.out.println("AdminController: Searching for user with email: " + email);
            Optional<User> userOpt = userService.findByEmail(email);
            users = userOpt.map(List::of).orElse(List.of());
            System.out.println("AdminController: Found " + users.size() + " users with email: " + email);
            if (!users.isEmpty()) {
                System.out.println("AdminController: Found user: " + users.get(0).getEmail() + " - " + users.get(0).getFirstName() + " " + users.get(0).getLastName());
            }
        } else {
            // Get all users
            System.out.println("AdminController: Getting all users");
            users = userService.getAllUsers();
            System.out.println("AdminController: Found " + users.size() + " total users");
        }
        
        List<Map<String, Object>> userList = users.stream()
            .map(this::convertUserToMap)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userList);
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        try {
            User user = userService.createUserFromAdmin(userData);
            return ResponseEntity.ok(convertUserToMap(user));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> userData) {
        User user = userService.updateUserFromAdmin(userId, userData);
        return ResponseEntity.ok(convertUserToMap(user));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long userId, @RequestBody Map<String, Boolean> statusData) {
        User user = userService.toggleUserStatus(userId, statusData.get("isActive"));
        return ResponseEntity.ok(convertUserToMap(user));
    }

    @GetMapping("/users/debug")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        System.out.println("AdminController: Debug endpoint called");
        List<User> allUsers = userService.getAllUsers();
        System.out.println("AdminController: Total users in database: " + allUsers.size());
        
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("totalUsers", allUsers.size());
        debugInfo.put("users", allUsers.stream()
            .map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("firstName", user.getFirstName());
                userInfo.put("lastName", user.getLastName());
                userInfo.put("role", user.getRole());
                return userInfo;
            })
            .collect(Collectors.toList()));
        
        return ResponseEntity.ok(debugInfo);
    }

    // Booking Management
    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        List<Map<String, Object>> bookingList = bookings.stream()
            .map(this::convertBookingToMap)
            .collect(Collectors.toList());
        return ResponseEntity.ok(bookingList);
    }

    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> bookingData) {
        Booking booking = bookingService.createBookingFromAdmin(bookingData);
        return ResponseEntity.ok(convertBookingToMap(booking));
    }

    @PutMapping("/bookings/{bookingId}")
    public ResponseEntity<Map<String, Object>> updateBooking(@PathVariable Long bookingId, @RequestBody Map<String, Object> bookingData) {
        Booking booking = bookingService.updateBookingFromAdmin(bookingId, bookingData);
        return ResponseEntity.ok(convertBookingToMap(booking));
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/bookings/{bookingId}/status")
    public ResponseEntity<Map<String, Object>> updateBookingStatus(@PathVariable Long bookingId, @RequestBody Map<String, String> statusData) {
        Booking booking = bookingService.updateBookingStatus(bookingId, statusData.get("status"));
        return ResponseEntity.ok(convertBookingToMap(booking));
    }

    // Lane Management
    @GetMapping("/lanes")
    public ResponseEntity<List<Map<String, Object>>> getAllLanes() {
        List<Lane> lanes = laneService.getAllLanes();
        List<Map<String, Object>> laneList = lanes.stream()
            .map(this::convertLaneToMap)
            .collect(Collectors.toList());
        return ResponseEntity.ok(laneList);
    }

    // Reports
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReportData(@RequestParam(defaultValue = "month") String period) {
        Map<String, Object> reportData = new HashMap<>();
        
        // Get period-specific data
        LocalDateTime startDate = getStartDateForPeriod(period);
        
        // Total bookings for period
        long totalBookings = bookingService.countBookingsInPeriod(startDate, LocalDateTime.now());
        reportData.put("totalBookings", totalBookings);
        
        // Total revenue for period
        BigDecimal totalRevenue = bookingService.getRevenueInPeriod(startDate, LocalDateTime.now());
        reportData.put("totalRevenue", totalRevenue);
        
        // Average booking value
        BigDecimal averageBookingValue = totalBookings > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        reportData.put("averageBookingValue", averageBookingValue);
        
        // Top performing lane
        Map<String, Object> topLane = bookingService.getTopPerformingLane(startDate, LocalDateTime.now());
        reportData.put("topLane", topLane.get("laneNumber"));
        reportData.put("topLaneBookings", topLane.get("bookingCount"));
        
        // Customer statistics
        long customerCount = userService.countCustomers();
        long newCustomers = userService.countNewCustomers(startDate);
        reportData.put("customerCount", customerCount);
        reportData.put("newCustomers", newCustomers);
        
        return ResponseEntity.ok(reportData);
    }

    @GetMapping("/reports/lane-performance")
    public ResponseEntity<List<Map<String, Object>>> getLanePerformance() {
        List<Map<String, Object>> lanePerformance = laneService.getLanePerformance();
        return ResponseEntity.ok(lanePerformance);
    }

    @GetMapping("/reports/top-customers")
    public ResponseEntity<List<Map<String, Object>>> getTopCustomers() {
        List<Map<String, Object>> topCustomers = userService.getTopCustomers();
        return ResponseEntity.ok(topCustomers);
    }

    // Settings
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        // Return default settings - in a real app, these would come from a settings service
        Map<String, Object> settings = new HashMap<>();
        settings.put("businessName", "iBowl Bowling Center");
        settings.put("businessHours", "10:00 AM - 10:00 PM");
        settings.put("lanePrice", "25.00");
        settings.put("maxPlayers", 6);
        settings.put("bookingAdvanceDays", 30);
        settings.put("autoConfirmBookings", true);
        settings.put("sendNotifications", true);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> settings) {
        // In a real app, this would save to a settings service
        return ResponseEntity.ok(settings);
    }

    // Helper methods
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("role", user.getRole().toString());
        userMap.put("phone", user.getPhone());
        userMap.put("loyaltyPoints", user.getLoyaltyPoints());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("isActive", true); // Default to true for now
        return userMap;
    }

    private Map<String, Object> convertBookingToMap(Booking booking) {
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("id", booking.getId());
        bookingMap.put("userId", booking.getUser().getId());
        bookingMap.put("laneId", booking.getLane().getId());
        bookingMap.put("date", booking.getStartTime().toLocalDate().toString());
        bookingMap.put("startTime", booking.getStartTime().toLocalTime().toString().substring(0, 5)); // Format as HH:mm
        bookingMap.put("endTime", booking.getEndTime().toLocalTime().toString().substring(0, 5)); // Format as HH:mm
        bookingMap.put("status", booking.getStatus().toString());
        bookingMap.put("totalPrice", booking.getTotalPrice());
        bookingMap.put("userEmail", booking.getUser().getEmail());
        bookingMap.put("userName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
        bookingMap.put("numberOfPlayers", booking.getPlayers());
        bookingMap.put("createdAt", booking.getStartTime().toString()); // Use startTime as createdAt for now
        return bookingMap;
    }

    private Map<String, Object> convertLaneToMap(Lane lane) {
        Map<String, Object> laneMap = new HashMap<>();
        laneMap.put("id", lane.getId());
        laneMap.put("number", lane.getNumber());
        laneMap.put("status", lane.getStatus().toString());
        laneMap.put("isActive", lane.getIsActive());
        return laneMap;
    }

    private LocalDateTime getStartDateForPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period.toLowerCase()) {
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            case "quarter":
                return now.minusMonths(3);
            case "year":
                return now.minusYears(1);
            default:
                return now.minusMonths(1);
        }
    }
} 