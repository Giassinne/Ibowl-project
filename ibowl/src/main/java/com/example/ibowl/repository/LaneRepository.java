package com.example.ibowl.repository;

import com.example.ibowl.entity.Lane;
import com.example.ibowl.entity.LaneStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaneRepository extends JpaRepository<Lane, Long> {
    List<Lane> findByStatus(LaneStatus status);
} 