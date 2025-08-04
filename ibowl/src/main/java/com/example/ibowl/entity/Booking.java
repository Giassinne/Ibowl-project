package com.example.ibowl.entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // Add this
import com.fasterxml.jackson.annotation.JsonIgnore; // Not strictly needed here, but good practice if you want to explicitly hide any field
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference // This side (Booking -> User) will be ignored during serialization
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id")
    @JsonBackReference // This side (Booking -> Lane) will be ignored during serialization
    private Lane lane;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private Integer players;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    @OneToMany(mappedBy = "booking")
    // If Game also has a Booking reference, you'll need JsonBackReference here too.
    // For now, let's assume Game doesn't reference Booking back or this is the desired serialization path.
    private List<Game> games;
}