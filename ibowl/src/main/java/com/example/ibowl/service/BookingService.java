package com.example.ibowl.service;

import com.example.ibowl.entity.Booking;
import com.example.ibowl.entity.BookingStatus;
import com.example.ibowl.entity.User;
import com.example.ibowl.entity.Lane;
import com.example.ibowl.repository.BookingRepository;
import com.example.ibowl.repository.UserRepository;
import com.example.ibowl.repository.LaneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LaneRepository laneRepository;

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

 
    public long countAllBookings() {
        return bookingRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        return bookingRepository.getTotalRevenue();
    }

    public long countActiveBookings() {
        return bookingRepository.countByStatus(BookingStatus.CONFIRMED);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking createBookingFromAdmin(Map<String, Object> bookingData) {
        System.out.println("BookingService: createBookingFromAdmin called with data: " + bookingData);
        
        Booking booking = new Booking();
        
        Long userId = Long.valueOf(bookingData.get("userId").toString());
        Long laneId = Long.valueOf(bookingData.get("laneId").toString());
        
        System.out.println("BookingService: Using userId: " + userId + ", laneId: " + laneId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Lane lane = laneRepository.findById(laneId)
            .orElseThrow(() -> new RuntimeException("Lane not found"));
        
        System.out.println("BookingService: Found user: " + user.getEmail() + " - " + user.getFirstName() + " " + user.getLastName());
        System.out.println("BookingService: Found lane: " + lane.getNumber());
        
        booking.setUser(user);
        booking.setLane(lane);
        booking.setStartTime(LocalDateTime.parse(bookingData.get("startTime").toString()));
        booking.setEndTime(LocalDateTime.parse(bookingData.get("endTime").toString()));
        booking.setPlayers(Integer.valueOf(bookingData.get("players").toString()));
        booking.setTotalPrice(new BigDecimal(bookingData.get("totalPrice").toString()));
        booking.setStatus(BookingStatus.valueOf(bookingData.get("status").toString()));
        
        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("BookingService: Saved booking with ID: " + savedBooking.getId() + " for user: " + savedBooking.getUser().getEmail());
        
        return savedBooking;
    }

    public Booking updateBookingFromAdmin(Long bookingId, Map<String, Object> bookingData) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (bookingData.containsKey("startTime")) {
            booking.setStartTime(LocalDateTime.parse(bookingData.get("startTime").toString()));
        }
        if (bookingData.containsKey("endTime")) {
            booking.setEndTime(LocalDateTime.parse(bookingData.get("endTime").toString()));
        }
        if (bookingData.containsKey("players")) {
            booking.setPlayers(Integer.valueOf(bookingData.get("players").toString()));
        }
        if (bookingData.containsKey("totalPrice")) {
            booking.setTotalPrice(new BigDecimal(bookingData.get("totalPrice").toString()));
        }
        if (bookingData.containsKey("status")) {
            booking.setStatus(BookingStatus.valueOf(bookingData.get("status").toString()));
        }
        
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    public Booking updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(BookingStatus.valueOf(status));
        return bookingRepository.save(booking);
    }

    public long countBookingsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.countByStartTimeBetween(startDate, endDate);
    }

    public BigDecimal getRevenueInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.getRevenueInPeriod(startDate, endDate);
    }

    public Map<String, Object> getTopPerformingLane(LocalDateTime startDate, LocalDateTime endDate) {
        Object[] result = bookingRepository.getTopPerformingLane(startDate, endDate);
        Map<String, Object> topLane = new HashMap<>();
        topLane.put("laneNumber", result[0]);
        topLane.put("bookingCount", result[1]);
        return topLane;
    }
} 