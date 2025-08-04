package com.example.ibowl.service;

import com.example.ibowl.entity.Lane;
import com.example.ibowl.repository.LaneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class LaneService {
    @Autowired
    private LaneRepository laneRepository;

    public List<Lane> findAll() {
        return laneRepository.findAll();
    }

    public Optional<Lane> findById(Long id) {
        return laneRepository.findById(id);
    }

    public Lane save(Lane lane) {
        return laneRepository.save(lane);
    }

    // Admin methods
    public List<Lane> getAllLanes() {
        return laneRepository.findAll();
    }

    public List<Map<String, Object>> getLanePerformance() {
        List<Lane> lanes = laneRepository.findAll();
        return lanes.stream()
            .map(lane -> {
                Map<String, Object> laneMap = new HashMap<>();
                laneMap.put("laneNumber", lane.getNumber());
                laneMap.put("status", lane.getStatus().toString());
                laneMap.put("isActive", lane.getIsActive());
                laneMap.put("bookingCount", 0); // This would be calculated from bookings
                laneMap.put("utilization", 0.0); // This would be calculated from bookings
                return laneMap;
            })
            .toList();
    }
} 