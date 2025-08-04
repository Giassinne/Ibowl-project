package com.example.ibowl.controller;

import com.example.ibowl.entity.Lane;
import com.example.ibowl.service.LaneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lanes")
public class LaneController {
    @Autowired
    private LaneService laneService;

    @GetMapping
    public ResponseEntity<List<Lane>> getAllLanes() {
        return ResponseEntity.ok(laneService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lane> getLane(@PathVariable Long id) {
        return laneService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Lane> createLane(@RequestBody Lane lane) {
        return ResponseEntity.ok(laneService.save(lane));
    }
} 