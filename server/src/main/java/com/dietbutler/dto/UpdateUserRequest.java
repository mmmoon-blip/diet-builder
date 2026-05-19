package com.dietbutler.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer age;
    private Double height;
    private Double targetWeight;
    private Double targetFat;
    private Double basicMetabolism;
    private String dietaryTaboo;
    private LocalTime sleepStart;
    private LocalTime sleepEnd;
    private LocalDate startWeightDate;
    private Integer weightLossPeriod;
    private Integer reminderIntervalHours;
    // ============ 用户类型 ============
    private String userType;               // weight_loss, shaping, maintenance, muscle_gain
    // ============ 体质标签 ============
    private String constitutionTags;        // 易水肿,代谢低,碳水敏感,压力胖,熬夜体质,宫寒,易胖体质,肌肉量低,膝盖不好
    // ============ 运动习惯 ============
    private Integer exerciseFrequency;
    private String exercisePreference;
    private Boolean hasKneeIssue;
    private String fitnessLevel;
    // ============ 饮食习惯 ============
    private String dietPreference;
    private Boolean breakfastHabit;
    private String mealTimes;
    // ============ 生活状态 ============
    private Integer workPressure;
    private Integer waterIntake;
    private Integer standingHours;
    // ============ 目标详情 ============
    private String targetAreas;
}
