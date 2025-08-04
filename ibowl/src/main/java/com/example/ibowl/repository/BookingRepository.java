package com.example.ibowl.repository;

import com.example.ibowl.entity.Booking;
import com.example.ibowl.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Admin methods
    long countByStatus(BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.startTime BETWEEN :startDate AND :endDate")
    long countByStartTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.startTime BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b.lane.number, COUNT(b) FROM Booking b WHERE b.startTime BETWEEN :startDate AND :endDate GROUP BY b.lane.number ORDER BY COUNT(b) DESC")
    Object[] getTopPerformingLane(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 