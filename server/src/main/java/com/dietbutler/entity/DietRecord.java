package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "diet_record")
public class DietRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String mealType;       // 早餐/午餐/晚餐/加餐

    private String foods;          // 食物描述

    private Integer calories;      // 摄入卡路里

    private String imageUrl;       // 图片URL

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