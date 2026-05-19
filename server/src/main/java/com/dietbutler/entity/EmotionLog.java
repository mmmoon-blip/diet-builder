package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情绪日志表
 * 记录用户情绪波动，用于AI情绪干预
 */
@Data
@Entity
@Table(name = "emotion_log")
public class EmotionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate recordDate;    // 记录日期

    private String emotion;          // 情绪类型: happy/calm/anxious/frustrated/binged/overeat

    private Integer level;          // 情绪强度 1-5

    private String emotionTrigger;     // 触发原因（可选）

    private String note;            // 备注/AI干预记录

    private Boolean aiIntervened;  // AI是否干预

    private String aiResponse;      // AI干预内容

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}