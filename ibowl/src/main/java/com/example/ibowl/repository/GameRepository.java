package com.example.ibowl.repository;

import com.example.ibowl.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
} 