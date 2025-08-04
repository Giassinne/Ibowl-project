package com.example.ibowl.controller;

import com.example.ibowl.dto.request.BookingRequest;
import com.example.ibowl.dto.response.BookingResponse;
import com.example.ibowl.entity.Booking;
import com.example.ibowl.entity.Lane;
import com.example.ibowl.entity.User;
import com.example.ibowl.service.BookingService;
import com.example.ibowl.service.LaneService;
import com.example.ibowl.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private LaneService laneService;

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<Booking> bookings = bookingService.findAll();
        List<BookingResponse> responses = bookings.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        return bookingService.findById(id)
                .map(booking -> ResponseEntity.ok(toResponse(booking)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        User user = userService.findById(request.getUserId()).orElseThrow();
        Lane lane = laneService.findById(request.getLaneId()).orElseThrow();
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setLane(lane);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPlayers(request.getPlayers());
        booking.setTotalPrice(request.getTotalPrice());
        booking.setStatus(request.getStatus() != null ? com.example.ibowl.entity.BookingStatus.valueOf(request.getStatus()) : com.example.ibowl.entity.BookingStatus.PENDING);
        Booking saved = bookingService.save(booking);
        return ResponseEntity.ok(toResponse(saved));
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse resp = new BookingResponse();
        resp.setId(booking.getId());
        resp.setUserId(booking.getUser() != null ? booking.getUser().getId() : null);
        resp.setLaneId(booking.getLane() != null ? booking.getLane().getId() : null);
        resp.setStartTime(booking.getStartTime());
        resp.setEndTime(booking.getEndTime());
        resp.setPlayers(booking.getPlayers());
        resp.setTotalPrice(booking.getTotalPrice());
        resp.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        return resp;
    }
} 