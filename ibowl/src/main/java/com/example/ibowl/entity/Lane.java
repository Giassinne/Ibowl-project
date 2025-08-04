package com.example.ibowl.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Add this
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Entity
@Table(name = "lanes")
@Getter
@Setter
public class Lane {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer number;

    @Enumerated(EnumType.STRING)
    private LaneStatus status = LaneStatus.AVAILABLE;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "lane")
    @JsonManagedReference // This side (Lane -> Bookings) will be serialized
    private List<Booking> bookings;
}