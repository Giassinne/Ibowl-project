package com.example.ibowl.repository;

import com.example.ibowl.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
} 