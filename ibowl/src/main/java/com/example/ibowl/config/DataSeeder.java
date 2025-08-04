package com.example.ibowl.config;

import com.example.ibowl.entity.*;
import com.example.ibowl.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LaneRepository laneRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // Only seed if database is empty
        if (userRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        System.out.println("🌱 Seeding database with sample data...");

        // Create lanes
        List<Lane> lanes = createLanes();
        
        // Create rooms
        List<Room> rooms = createRooms();
        
        // Create users
        List<User> users = createUsers();
        
        // Create bookings
        List<Booking> bookings = createBookings(users, lanes);
        
        // Create games
        createGames(bookings);

        System.out.println("✅ Database seeded successfully!");
        System.out.println("📊 Created:");
        System.out.println("   - " + lanes.size() + " lanes");
        System.out.println("   - " + rooms.size() + " rooms");
        System.out.println("   - " + users.size() + " users");
        System.out.println("   - " + bookings.size() + " bookings");
        System.out.println("   - " + gameRepository.count() + " games");
    }

    private List<Lane> createLanes() {
        List<Lane> lanes = Arrays.asList(
            createLane(1, LaneStatus.AVAILABLE),
            createLane(2, LaneStatus.AVAILABLE),
            createLane(3, LaneStatus.AVAILABLE),
            createLane(4, LaneStatus.AVAILABLE),
            createLane(5, LaneStatus.AVAILABLE),
            createLane(6, LaneStatus.AVAILABLE),
            createLane(7, LaneStatus.MAINTENANCE),
            createLane(8, LaneStatus.AVAILABLE)
        );
        
        return laneRepository.saveAll(lanes);
    }

    private Lane createLane(int number, LaneStatus status) {
        Lane lane = new Lane();
        lane.setNumber(number);
        lane.setStatus(status);
        lane.setIsActive(true);
        return lane;
    }

    private List<Room> createRooms() {
        List<Room> rooms = Arrays.asList(
            createRoom("Party Room 1", 20, "Perfect for birthday parties and events"),
            createRoom("VIP Lounge", 12, "Premium experience with private service"),
            createRoom("Conference Room", 30, "Business meetings and corporate events"),
            createRoom("Arcade Area", 50, "Gaming zone with latest arcade machines")
        );
        
        return roomRepository.saveAll(rooms);
    }

    private Room createRoom(String name, int capacity, String description) {
        Room room = new Room();
        room.setName(name);
        room.setCapacity(capacity);
        room.setDescription(description);
        room.setIsActive(true);
        return room;
    }

    private List<User> createUsers() {
        List<User> users = Arrays.asList(
            // Admin users
            createUser("admin@ibowl.com", "admin123", "Admin", "User", "+1-555-0100", Role.ADMIN, 0),
            createUser("manager@ibowl.com", "manager123", "Sarah", "Manager", "+1-555-0101", Role.ADMIN, 0),
            
            // Staff users
            createUser("staff1@ibowl.com", "staff123", "Mike", "Johnson", "+1-555-0200", Role.STAFF, 0),
            createUser("staff2@ibowl.com", "staff123", "Lisa", "Chen", "+1-555-0201", Role.STAFF, 0),
            createUser("staff3@ibowl.com", "staff123", "David", "Wilson", "+1-555-0202", Role.STAFF, 0),
            
            // Customer users
            createUser("john.doe@email.com", "password123", "John", "Doe", "+1-555-1000", Role.CUSTOMER, 150),
            createUser("jane.smith@email.com", "password123", "Jane", "Smith", "+1-555-1001", Role.CUSTOMER, 85),
            createUser("bob.johnson@email.com", "password123", "Bob", "Johnson", "+1-555-1002", Role.CUSTOMER, 220),
            createUser("alice.brown@email.com", "password123", "Alice", "Brown", "+1-555-1003", Role.CUSTOMER, 95),
            createUser("charlie.wilson@email.com", "password123", "Charlie", "Wilson", "+1-555-1004", Role.CUSTOMER, 180),
            createUser("emma.davis@email.com", "password123", "Emma", "Davis", "+1-555-1005", Role.CUSTOMER, 120),
            createUser("michael.garcia@email.com", "password123", "Michael", "Garcia", "+1-555-1006", Role.CUSTOMER, 75),
            createUser("sophia.rodriguez@email.com", "password123", "Sophia", "Rodriguez", "+1-555-1007", Role.CUSTOMER, 200),
            createUser("william.martinez@email.com", "password123", "William", "Martinez", "+1-555-1008", Role.CUSTOMER, 160),
            createUser("olivia.anderson@email.com", "password123", "Olivia", "Anderson", "+1-555-1009", Role.CUSTOMER, 110),
            createUser("james.taylor@email.com", "password123", "James", "Taylor", "+1-555-1010", Role.CUSTOMER, 140),
            createUser("ava.thomas@email.com", "password123", "Ava", "Thomas", "+1-555-1011", Role.CUSTOMER, 90),
            createUser("benjamin.jackson@email.com", "password123", "Benjamin", "Jackson", "+1-555-1012", Role.CUSTOMER, 175),
            createUser("mia.white@email.com", "password123", "Mia", "White", "+1-555-1013", Role.CUSTOMER, 130),
            createUser("ethan.harris@email.com", "password123", "Ethan", "Harris", "+1-555-1014", Role.CUSTOMER, 105),
            createUser("isabella.clark@email.com", "password123", "Isabella", "Clark", "+1-555-1015", Role.CUSTOMER, 190),
            createUser("noah.lewis@email.com", "password123", "Noah", "Lewis", "+1-555-1016", Role.CUSTOMER, 145),
            createUser("sophia.lee@email.com", "password123", "Sophia", "Lee", "+1-555-1017", Role.CUSTOMER, 80),
            createUser("logan.walker@email.com", "password123", "Logan", "Walker", "+1-555-1018", Role.CUSTOMER, 165),
            createUser("chloe.hall@email.com", "password123", "Chloe", "Hall", "+1-555-1019", Role.CUSTOMER, 115),
            createUser("mason.allen@email.com", "password123", "Mason", "Allen", "+1-555-1020", Role.CUSTOMER, 135)
        );
        
        return userRepository.saveAll(users);
    }

    private User createUser(String email, String password, String firstName, String lastName, String phone, Role role, int loyaltyPoints) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setRole(role);
        user.setLoyaltyPoints(loyaltyPoints);
        return user;
    }

    private List<Booking> createBookings(List<User> users, List<Lane> lanes) {
        List<Booking> bookings = new ArrayList<>();
        
        // Get customer users only
        List<User> customers = users.stream()
            .filter(user -> user.getRole() == Role.CUSTOMER)
            .toList();
        
        // Get available lanes
        List<Lane> availableLanes = laneRepository.findByStatus(LaneStatus.AVAILABLE);
        
        // Create bookings for the past 30 days and next 30 days
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 50; i++) {
            // Random date within ±30 days
            LocalDateTime bookingDate = now.plusDays(random.nextInt(61) - 30);
            
            // Random time between 10 AM and 10 PM
            int hour = 10 + random.nextInt(12);
            int minute = random.nextInt(4) * 15; // 0, 15, 30, 45
            LocalDateTime startTime = bookingDate.with(LocalTime.of(hour, minute));
            LocalDateTime endTime = startTime.plusHours(1 + random.nextInt(3)); // 1-3 hours
            
            // Random customer and lane
            User customer = customers.get(random.nextInt(customers.size()));
            Lane lane = availableLanes.get(random.nextInt(availableLanes.size()));
            
            // Random number of players (1-6)
            int players = 1 + random.nextInt(6);
            
            // Calculate price based on duration and players
            long durationHours = java.time.Duration.between(startTime, endTime).toHours();
            BigDecimal basePrice = new BigDecimal("25.00"); // $25 per hour
            BigDecimal totalPrice = basePrice.multiply(BigDecimal.valueOf(durationHours));
            
            // Random status (mostly confirmed, some pending, few cancelled)
            BookingStatus status;
            int statusRoll = random.nextInt(100);
            if (statusRoll < 70) {
                status = BookingStatus.CONFIRMED;
            } else if (statusRoll < 90) {
                status = BookingStatus.PENDING;
            } else {
                status = BookingStatus.CANCELLED;
            }
            
            Booking booking = new Booking();
            booking.setUser(customer);
            booking.setLane(lane);
            booking.setStartTime(startTime);
            booking.setEndTime(endTime);
            booking.setPlayers(players);
            booking.setTotalPrice(totalPrice);
            booking.setStatus(status);
            
            bookings.add(booking);
        }
        
        return bookingRepository.saveAll(bookings);
    }

    private void createGames(List<Booking> bookings) {
        // Only create games for confirmed bookings
        List<Booking> confirmedBookings = bookings.stream()
            .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
            .toList();
        
        for (Booking booking : confirmedBookings) {
            // Create 1-3 games per booking
            int numGames = 1 + random.nextInt(3);
            
            for (int i = 0; i < numGames; i++) {
                Game game = new Game();
                game.setBooking(booking);
                game.setGameNumber(i + 1);
                game.setPlayer1Score(random.nextInt(300)); // 0-299
                game.setPlayer2Score(random.nextInt(300));
                game.setPlayer3Score(random.nextInt(300));
                game.setPlayer4Score(random.nextInt(300));
                game.setPlayer5Score(random.nextInt(300));
                game.setPlayer6Score(random.nextInt(300));
                
                gameRepository.save(game);
            }
        }
    }
} 