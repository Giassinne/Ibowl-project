package com.example.ibowl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "games")
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "game_number")
    private Integer gameNumber;

    @Column(name = "player1_score")
    private Integer player1Score;

    @Column(name = "player2_score")
    private Integer player2Score;

    @Column(name = "player3_score")
    private Integer player3Score;

    @Column(name = "player4_score")
    private Integer player4Score;

    @Column(name = "player5_score")
    private Integer player5Score;

    @Column(name = "player6_score")
    private Integer player6Score;
} 