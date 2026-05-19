package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "exercise_record")
public class ExerciseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String type;           // 运动类型

    private Integer duration;      // 时长（分钟）

    private Integer calories;      // 消耗卡路里

    private LocalDate recordDate;   // 记录日期

    private LocalTime startTime;   // 开始时间（可选）

    private LocalTime endTime;     // 结束时间（可选）

    private String note;           // 备注

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}