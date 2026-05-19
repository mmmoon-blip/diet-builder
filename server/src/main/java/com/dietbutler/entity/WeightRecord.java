package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "weight_records")
public class WeightRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double weight;

    private String note;

    @Column(nullable = false)
    private LocalDate recordDate = LocalDate.now();

    // 作息时间：记录当天的睡眠时段
    private LocalTime sleepStart;  // 睡眠开始时间
    private LocalTime sleepEnd;    // 起床时间

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
