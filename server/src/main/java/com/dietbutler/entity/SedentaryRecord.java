package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 久坐记录表
 * 记录用户每日久坐情况，配合提醒功能
 */
@Data
@Entity
@Table(name = "sedentary_record")
public class SedentaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate recordDate;    // 记录日期

    private Integer standCount;      // 站立次数

    private Integer totalSedentaryMinutes; // 总久坐分钟数

    private Integer goalMinutes;     // 目标久坐上限（分钟）

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}