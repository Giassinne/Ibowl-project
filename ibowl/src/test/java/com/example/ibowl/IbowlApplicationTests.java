package com.example.ibowl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.ibowl.entity.*;
import com.example.ibowl.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.example.ibowl.service.UserService;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IbowlApplicationTests {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private LaneRepository laneRepository;
	@Autowired
	private RoomRepository roomRepository;
	@Autowired
	private BookingRepository bookingRepository;
	@Autowired
	private GameRepository gameRepository;
	@Autowired
	private UserService userService;

	@Test
	void contextLoads() {
	}

	@Test
	void testReadAllUsers() {
		List<User> users = userRepository.findAll();
		System.out.println("Users: " + users.size());
	}

	@Test
	void testReadAllLanes() {
		List<Lane> lanes = laneRepository.findAll();
		System.out.println("Lanes: " + lanes.size());
	}

	@Test
	void testReadAllRooms() {
		List<Room> rooms = roomRepository.findAll();
		System.out.println("Rooms: " + rooms.size());
	}

	@Test
	void testReadAllBookings() {
		List<Booking> bookings = bookingRepository.findAll();
		System.out.println("Bookings: " + bookings.size());
	}

	@Test
	void testReadAllGames() {
		List<Game> games = gameRepository.findAll();
		System.out.println("Games: " + games.size());
	}

	@Test
	void testFindUserByEmail() {
		// This email should exist if the database is seeded as in DataSeeder
		String knownEmail = "admin@ibowl.com";
		Optional<User> userOpt = userService.findByEmail(knownEmail);
		assertTrue(userOpt.isPresent(), "User with email 'admin@ibowl.com' should exist");
		userOpt.ifPresent(user -> assertEquals(knownEmail, user.getEmail()));
	}

}
