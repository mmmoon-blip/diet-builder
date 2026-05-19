package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "body_measurements")
public class BodyMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate recordDate = LocalDate.now();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 腰围 (cm)
    private Double waist;

    // 臀围 (cm)
    private Double hip;

    // 胸围 (cm)
    private Double chest;

    // 大臂围 (cm)
    private Double upperArm;



    // 小臂围 (cm)
    private Double forearm;

    // 大腿围 (cm)
    private Double thigh;

    // 小腿围 (cm)
    private Double calf;
}
